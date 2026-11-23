#!/usr/bin/env bash
# =============================================================================
# Livraison du SDK : cœur du bouton « Release » (workflow release.yml)
# -----------------------------------------------------------------------------
# Adapté du composant GitLab payline/ci-shared/templates/plugin-release.yml.
#
# CE QUE FAIT LE SCRIPT
#   1. sur la tête de develop, un commit « Release X »          (version=X) ;
#   2. par-dessus, un commit « Préparation Y-SNAPSHOT »         (version=Y-SNAPSHOT) ;
#   3. UN push atomique : le 1er commit vers main, le 2nd vers develop.
#
#   Les deux pushes sont des avances rapides : ni rebase ni merge, donc aucun
#   conflit possible, et les deux branches avancent ensemble ou pas du tout.
#   Condition : main doit être un ancêtre de develop. Sinon le script REFUSE ;
#   il ne force jamais un push.
#
#   Le tag v<X> n'est PAS posé ici : c'est le job de publication de main
#   (github-action.yml) qui le pose sur le commit livré, une fois la
#   publication Maven Central réussie. Le script vérifie seulement qu'il
#   n'existe pas encore.
#
# ENTRÉES (variables d'environnement)
#   RELEASE_VERSION      version à livrer (vide = version courante sans -SNAPSHOT)
#   NEXT_VERSION         version suivante (vide = dernier chiffre + 1, suffixe -SNAPSHOT)
#   RELEASE_DRY_RUN      "true" = tout vérifier et calculer, sans rien pousser
#   RELEASE_REMOTE       dépôt distant                      (défaut : origin)
#   RELEASE_MAIN_BRANCH  branche qui reçoit la release      (défaut : main)
#   RELEASE_DEV_BRANCH   branche d'intégration              (défaut : develop)
#   RELEASE_VERSION_FILE fichier portant `version=…`        (défaut : gradle.properties)
#   RELEASE_TAG_PREFIX   préfixe du tag                     (défaut : v)
#   GIT_AUTHOR_* / GIT_COMMITTER_*  auteur des deux commits (la personne qui a cliqué)
#
# SORTIES (si GITHUB_OUTPUT est défini)
#   release_version, next_version, tag, release_sha, next_sha
#
# ESSAI LOCAL
#   Le script n'utilise que git et sh : il peut être lancé contre un dépôt nu
#   local (RELEASE_REMOTE=/chemin/vers/depot.git).
# =============================================================================
set -euo pipefail

REMOTE="${RELEASE_REMOTE:-origin}"
MAIN_BRANCH="${RELEASE_MAIN_BRANCH:-main}"
DEV_BRANCH="${RELEASE_DEV_BRANCH:-develop}"
VERSION_FILE="${RELEASE_VERSION_FILE:-gradle.properties}"
TAG_PREFIX="${RELEASE_TAG_PREFIX:-v}"
DRY_RUN="${RELEASE_DRY_RUN:-false}"

fail() { echo "::error::LIVRAISON REFUSÉE : $1" >&2; exit 1; }

output() {
  if [ -n "${GITHUB_OUTPUT:-}" ]; then echo "$1=$2" >> "${GITHUB_OUTPUT}"; fi
}

git fetch --quiet "${REMOTE}" \
  "+refs/heads/${MAIN_BRANCH}:refs/remotes/release/${MAIN_BRANCH}" \
  "+refs/heads/${DEV_BRANCH}:refs/remotes/release/${DEV_BRANCH}"

# --- Garde-fous : tout est vérifié avant la première écriture ---------------
HEAD_SHA=$(git rev-parse HEAD)
DEV_SHA=$(git rev-parse "refs/remotes/release/${DEV_BRANCH}")
[ "${HEAD_SHA}" = "${DEV_SHA}" ] || \
  fail "${DEV_BRANCH} a avancé (${HEAD_SHA} n'est plus sa tête, c'est ${DEV_SHA}). Attendre le pipeline du nouveau commit puis relancer."

git merge-base --is-ancestor "refs/remotes/release/${MAIN_BRANCH}" HEAD || \
  fail "${MAIN_BRANCH} contient des commits absents de ${DEV_BRANCH}. Les intégrer d'abord dans ${DEV_BRANCH} ; aucun push forcé ne sera fait."

[ "$(grep -c '^version[[:space:]]*=' "${VERSION_FILE}")" = "1" ] || \
  fail "${VERSION_FILE} doit contenir exactement une ligne 'version=…'."
CURRENT=$(sed -n 's/^version[[:space:]]*=[[:space:]]*//p' "${VERSION_FILE}" | tr -d '\r' | sed 's/[[:space:]]*$//')

case "${CURRENT}" in
  *-SNAPSHOT) ;;
  *) fail "la version courante de ${DEV_BRANCH} (${CURRENT}) n'est pas un SNAPSHOT." ;;
esac

RELEASE="${RELEASE_VERSION:-${CURRENT%-SNAPSHOT}}"
printf '%s' "${RELEASE}" | grep -Eq '^[0-9]+(\.[0-9]+)*$' || \
  fail "version de release invalide : '${RELEASE}' (attendu : des nombres séparés par des points, ex. 1.0.10)."

if [ -n "${NEXT_VERSION:-}" ]; then
  NEXT="${NEXT_VERSION}"
else
  PREFIX="${RELEASE%.*}"
  # Zéros de tête retirés : `08` serait lu comme un nombre octal invalide.
  LAST=$(printf '%s' "${RELEASE##*.}" | sed 's/^0*\([0-9]\)/\1/')
  if [ "${PREFIX}" = "${RELEASE}" ]; then NEXT="$((LAST + 1))-SNAPSHOT"; else NEXT="${PREFIX}.$((LAST + 1))-SNAPSHOT"; fi
fi
printf '%s' "${NEXT}" | grep -Eq '^[0-9]+(\.[0-9]+)*-SNAPSHOT$' || \
  fail "version suivante invalide : '${NEXT}' (attendu : nombres séparés par des points, suffixe -SNAPSHOT, ex. 1.0.11-SNAPSHOT)."

# La version suivante doit être strictement supérieure à la release.
NEXT_NUM="${NEXT%-SNAPSHOT}"
[ "${NEXT_NUM}" != "${RELEASE}" ] && \
  [ "$(printf '%s\n%s\n' "${RELEASE}" "${NEXT_NUM}" | sort -V | tail -n 1)" = "${NEXT_NUM}" ] || \
  fail "la version suivante (${NEXT}) doit être supérieure à la release (${RELEASE})."

TAG="${TAG_PREFIX}${RELEASE}"
[ -z "$(git ls-remote --tags "${REMOTE}" "refs/tags/${TAG}")" ] || \
  fail "le tag ${TAG} existe déjà : la version ${RELEASE} a déjà été livrée."

echo "Livraison : ${CURRENT} -> ${RELEASE} sur ${MAIN_BRANCH}, puis ${NEXT} sur ${DEV_BRANCH} (tag ${TAG} posé à la publication)"

# --- Deux commits -----------------------------------------------------------
# `[^[:cntrl:]]*` s'arrête avant un éventuel retour chariot : les fins de ligne
# du fichier sont conservées.
set_version() {
  sed "s/^\(version[[:space:]]*=[[:space:]]*\)[^[:cntrl:]]*/\1$1/" "${VERSION_FILE}" > "${VERSION_FILE}.release-tmp"
  cat "${VERSION_FILE}.release-tmp" > "${VERSION_FILE}"
  rm -f "${VERSION_FILE}.release-tmp"
  git add -- "${VERSION_FILE}"
  git commit --quiet -m "$2"
}

set_version "${RELEASE}" "Release ${RELEASE}"
RELEASE_SHA=$(git rev-parse HEAD)
set_version "${NEXT}" "Préparation ${NEXT}"
NEXT_SHA=$(git rev-parse HEAD)

output release_version "${RELEASE}"
output next_version "${NEXT}"
output tag "${TAG}"
output release_sha "${RELEASE_SHA}"
output next_sha "${NEXT_SHA}"

# --- Un push atomique : les deux branches avancent, ou aucune ---------------
if [ "${DRY_RUN}" = "true" ]; then
  echo "À BLANC : rien n'est poussé. Auraient avancé : ${MAIN_BRANCH} -> ${RELEASE_SHA}, ${DEV_BRANCH} -> ${NEXT_SHA}"
else
  git push --quiet --atomic "${REMOTE}" \
    "${RELEASE_SHA}:refs/heads/${MAIN_BRANCH}" \
    "${NEXT_SHA}:refs/heads/${DEV_BRANCH}" || \
    fail "push refusé, rien n'a été modifié. La clé de déploiement a-t-elle l'écriture, et figure-t-elle dans les exceptions des règles de ${MAIN_BRANCH} et ${DEV_BRANCH} ?"
  echo "Poussé : ${MAIN_BRANCH} = ${RELEASE_SHA} (${RELEASE}), ${DEV_BRANCH} = ${NEXT_SHA} (${NEXT})"
fi
