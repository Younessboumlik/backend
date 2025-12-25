# Configuration Lombok dans IntelliJ IDEA

## Problème résolu dans pom.xml
✅ Version de Lombok spécifiée explicitement
✅ Configuration du maven-compiler-plugin améliorée
✅ Scope de Lombok changé en `provided`

## Étapes pour configurer IntelliJ IDEA

### 1. Installer le plugin Lombok
1. Ouvrez IntelliJ IDEA
2. Allez dans **File** → **Settings** (ou `Ctrl+Alt+S` sur Windows)
3. Dans la barre de recherche, tapez "Plugins"
4. Cliquez sur **Plugins** dans le menu de gauche
5. Cliquez sur **Marketplace** (en haut)
6. Recherchez "**Lombok**"
7. Installez le plugin **"Lombok"** (par Michail Plushnikov)
8. Cliquez sur **Apply** puis **OK**
9. **Redémarrez IntelliJ IDEA** (important !)

### 2. Activer l'Annotation Processing
1. Ouvrez **File** → **Settings** (`Ctrl+Alt+S`)
2. Allez dans **Build, Execution, Deployment** → **Compiler** → **Annotation Processors**
3. Cochez **"Enable annotation processing"**
4. Cliquez sur **Apply** puis **OK**

### 3. Recharger le projet Maven
1. Ouvrez la fenêtre **Maven** (généralement à droite, ou **View** → **Tool Windows** → **Maven**)
2. Cliquez sur l'icône **"Reload All Maven Projects"** (flèche circulaire)
   - Ou faites un clic droit sur le projet → **Maven** → **Reload project**

### 4. Invalider les caches (si nécessaire)
Si le problème persiste :
1. **File** → **Invalidate Caches...**
2. Cochez toutes les options
3. Cliquez sur **Invalidate and Restart**
4. IntelliJ redémarrera automatiquement

### 5. Vérifier que ça fonctionne
1. Ouvrez une classe avec des annotations Lombok (ex: `User.java`)
2. Placez votre curseur sur une annotation comme `@Getter` ou `@Setter`
3. Appuyez sur `Alt+Enter` → Lombok devrait être reconnu
4. Les getters/setters devraient être disponibles dans l'autocomplétion

## Vérification rapide
Dans une classe avec `@Getter @Setter`, essayez d'écrire :
```java
User user = new User();
user.setEmail("test@test.com");  // Devrait fonctionner
String email = user.getEmail();  // Devrait fonctionner
```

## Si le problème persiste

### Option 1 : Vérifier la version de Java
- Assurez-vous que le projet utilise Java 17 (comme configuré dans pom.xml)
- **File** → **Project Structure** → **Project** → Vérifiez **SDK** et **Language level**

### Option 2 : Rebuild le projet
- **Build** → **Rebuild Project**

### Option 3 : Nettoyer et recompiler
Dans le terminal IntelliJ ou PowerShell :
```bash
mvn clean compile
```

### Option 4 : Vérifier les dépendances Maven
Dans la fenêtre Maven :
- Développez **Dependencies**
- Vérifiez que `lombok` apparaît bien

## Notes importantes
- ⚠️ Lombok génère le code à la **compilation**, pas à l'édition
- Les getters/setters n'apparaissent pas dans le code source, mais sont disponibles à l'exécution
- IntelliJ peut les "voir" grâce au plugin Lombok qui analyse les annotations


