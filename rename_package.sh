#!/bin/bash
# rename_package.sh — Racine du projet NexusCare-Parental

set -euo pipefail  # Exit on error, undefined vars, pipe failures

OLD_PKG_DOT="com.mansourappdevelopment.androidapp.kidsafe"
NEW_PKG_DOT="cm.nexuscare.parental"
OLD_PKG_PATH="com/mansourappdevelopment/androidapp/kidsafe"
NEW_PKG_PATH="cm/nexuscare/parental"

JAVA_SRC="Application/app/src/main/java/"

echo "🔍 Vérification de la source..."
if [ ! -d "$JAVA_SRC/$OLD_PKG_PATH" ]; then
    echo "❌ Dossier source introuvable : $JAVA_SRC/$OLD_PKG_PATH"
    echo "   Arborescence actuelle :"
    find "$JAVA_SRC" -type d | head -20
    exit 1
fi

echo "📁 Création de la nouvelle arborescence de packages..."
mkdir -p "$JAVA_SRC/$NEW_PKG_PATH"

echo "📋 Copie des fichiers source..."
find "$JAVA_SRC/$OLD_PKG_PATH" -type f \( -name "*.kt" -o -name "*.java" \) | while read -r f; do
    rel="${f#$JAVA_SRC/$OLD_PKG_PATH/}"
    target="$JAVA_SRC/$NEW_PKG_PATH/$rel"
    mkdir -p "$(dirname "$target")"
    cp "$f" "$target"
    echo "  ✓ $rel"
done

echo "🔄 Remplacement des déclarations de package dans les fichiers source..."
find "$JAVA_SRC/$NEW_PKG_PATH" -type f \( -name "*.kt" -o -name "*.java" \) \
    -exec sed -i "s|$OLD_PKG_DOT|$NEW_PKG_DOT|g" {} +

echo "📝 Mise à jour de AndroidManifest.xml..."
sed -i "s|$OLD_PKG_DOT|$NEW_PKG_DOT|g" Application/app/src/main/AndroidManifest.xml

echo "⚙️  Mise à jour des fichiers Gradle..."
find . \( -name "*.gradle" -o -name "*.gradle.kts" \) -not -path "*/build/*" \
    -exec sed -i "s|$OLD_PKG_DOT|$NEW_PKG_DOT|g" {} +

echo "🏷️  Mise à jour du nom de l'app dans strings.xml..."
sed -i 's|<string name="app_name">.*</string>|<string name="app_name">NexusCare</string>|g' \
    Application/app/src/main/res/values/strings.xml

echo ""
echo "✅ Renommage terminé."
echo ""
echo "⚠️  Vérification manuelle AVANT de supprimer l'ancien package :"
echo "   1. Ouvre VSCode et contrôle un fichier dans $JAVA_SRC/$NEW_PKG_PATH"
echo "   2. Vérifie que la première ligne dit : package $NEW_PKG_DOT"
echo "   3. Vérifie AndroidManifest.xml : package=\"$NEW_PKG_DOT\""
echo ""
echo "   Si tout est OK, supprime l'ancien package avec :"
echo "   rm -rf $JAVA_SRC/com"
echo ""
echo "📦 Ensuite, lance le build :"
echo "   ./gradlew assembleDebug 2>&1 | tee build.log"