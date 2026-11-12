# Spring AI Fast Track — Journal d'exercices

Journal des exercices réalisés dans le cadre du cours **Spring AI Fast Track**.

**Stack technique utilisée**
- Java 27
- Spring Boot 4.1 (4.1.2)
- Spring AI 2.0.1
- Maven (projet parent + modules)
- Docker Desktop avec Docker Model Runner
- Ollama
- Gemini (API gratuite de Google AI Studio)
- Git / GitHub
- IntelliJ IDEA

> Ce README documente uniquement les exercices réalisés et les informations effectivement vérifiées (logs, traces d'erreur, sorties de commandes, fichiers de configuration). Les exercices non encore réalisés, les pistes de test et les mesures non collectées ne sont pas présentés comme des travaux terminés. Les sections « en cours » signalent explicitement ce qui n'est pas encore validé.

---

## Structure du dépôt

Le dépôt est un projet Maven **parent** qui regroupe un module par série d'exercices.

```text
spring-ai-projects/        <- projet parent (packaging pom), versions communes
├── pom.xml                <- spring-boot-starter-parent + spring-ai-bom + <modules>
├── .gitignore
├── README.md
└── series-01/             <- module de la série 01
    ├── pom.xml            <- <parent> pointe vers spring-ai-projects
    └── src/
```

- Le parent ne contient ni code ni dépendance : il fixe les versions de Spring Boot et de Spring AI (`spring-ai-bom`).
- Chaque module déclare ses propres dépendances (starters Spring AI, web, tests).
- Les expériences qui remplacent un fournisseur dans un module existant sont isolées dans des branches Git nommées `exercise/<numéro>-<sujet>` (par exemple `exercise/1-11-two-models`).

---

## Série 01 — Say Hello to AI in Spring Boot

**Dossier :** `series-01/`

### Exercices réalisés et documentés

| Exercice | Réalisation |
|---|---|
| 1.1 | Projet généré avec Spring Initializr. Premier essai de démarrage sans clé API (voir la section 1.1). |
| 1.2 | Clé API Gemini lue depuis une variable d'environnement, jamais écrite en clair (voir la section 1.2). |
| 1.3 | Création de l'endpoint `GET /api/chat?message=...` avec `ChatClient`. |
| 1.4 | Affichage des beans Spring AI au démarrage avec `BeanLister` et activation des logs `debug=true`. |
| 1.5 | Estimation du coût de 10 000 requêtes courtes et longues. |
| 1.6 | Comparaison de l'utilisation de `ChatModel` + `Prompt` avec `ChatClient`. |
| 1.7 | Utilisation d'Ollama comme fournisseur local sans modifier le contrôleur. |
| 1.8 | Intégration de Docker Model Runner avec le starter OpenAI, configuration du `base-url` et utilisation d'une clé factice. |

### Exercices en cours (non validés)

| Exercice | État |
|---|---|
| 1.11 | Deux `ChatClient` avec `@Qualifier` : une première erreur de démarrage a été rencontrée et analysée (voir plus bas). |
| 1.13 | Profils Spring : un problème de dépendance de test est en cours d'analyse (voir plus bas). |

---

### Exercice 1.1 — Générer le projet et démarrer sans clé

**Objectif :** générer le projet avec Spring Initializr et observer le comportement de l'application sans clé API.

**Constat vérifié :** le premier démarrage a été fait sur un projet dont le classpath, lu dans la commande de lancement, ne contenait **aucun jar `spring-ai-*`**. L'application a démarré normalement. Ce résultat ne dit rien du comportement de Spring AI, puisque la bibliothèque n'était pas chargée.

**Leçon :** avant de chercher une erreur de configuration, vérifier le classpath ou le `pom.xml` et le projet réellement lancé.

**Comportement du cours non confirmé :** le cours indique qu'une application avec le starter OpenAI échoue au démarrage avec « API key must be set ». Cet échec n'a pas été observé avec Spring AI 2.0.1 : l'application a démarré sans erreur. La cause n'a pas été confirmée. Les hypothèses restantes sont une validation de la clé au moment de l'appel dans cette version, ou une clé déjà présente dans l'environnement. Le comportement dépend donc de la version utilisée, et la documentation de la version en service prime sur le cours.

---

### Exercice 1.2 — Sécuriser la clé API (Gemini)

**Objectif :** configurer la clé uniquement par variable d'environnement, sans jamais l'écrire dans un fichier du dépôt.

**Choix de fournisseur :** Gemini a été utilisé à la place d'OpenAI parce que Google AI Studio fournit une clé gratuite. Le starter utilisé est `spring-ai-starter-model-google-genai`.

#### Configuration utilisée

```yaml
spring:
  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY}
```

La valeur de la clé n'apparaît dans aucun fichier : le fichier de configuration ne contient que la référence à la variable.

#### Précautions appliquées

- Définir la variable `GEMINI_API_KEY` au niveau du compte Windows.
- Une variable créée après le lancement d'IntelliJ n'est pas vue par lui : fermer et rouvrir IntelliJ.
- Ne définir ni `project-id` ni `location` pour l'API gratuite : ces propriétés font basculer le client en mode Vertex AI.
- Ne pas laisser de clé dans l'historique du terminal ni dans un message partagé.

#### Contrôle avant chaque commit

```bash
git add .
git grep --cached -n "AIza"
echo $?
```

Aucune ligne ne doit s'afficher et le code de retour doit être `1`. L'option `--cached` doit être placée **avant** le motif recherché.

Le `.gitignore` généré par Spring Initializr ne couvre pas les fichiers de secrets. Les lignes suivantes y ont été recommandées :

```text
.env
*.env
application-local.yaml
application-local.yml
application-local.properties
```

---

### Exercice 1.3 — Premier endpoint de chat

**Objectif :** exposer `GET /api/chat?message=...` avec un `ChatClient` construit depuis le `ChatClient.Builder` créé automatiquement par Spring AI.

```java
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/api/chat")
    public String chat(@RequestParam(name = "message") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
```

**Ce que montre la trace d'erreur :** l'appel ne part vers le modèle que lorsque la méthode finale (`content()`) est appelée. Les méthodes `prompt()`, `user()` et `call()` préparent la requête. La trace passe par la chaîne d'advisors (`ToolCallingAdvisor`, `ChatModelCallAdvisor`) puis par `GoogleGenAiChatModel`.

**Le contrôleur ne mentionne aucun nom de fournisseur :** il ne dépend que de `ChatClient`. Changer de fournisseur modifie le starter et la configuration, pas ce fichier.

#### Erreur rencontrée : HTTP 500 dû à un modèle Gemini retiré

**Symptôme :** l'endpoint renvoyait une erreur 500 `Failed to generate content`.

**Cause (dernière ligne `Caused by` de la trace) :** Google répondait 404 : le modèle `gemini-2.5-flash`, utilisé par défaut par le starter, n'était plus disponible pour les nouveaux utilisateurs, et le message recommandait `gemini-3.8-flash`. Le 500 est celui de l'application, qui ne gérait pas l'exception.

**Correction :** choisir le modèle explicitement dans la configuration.

```yaml
spring:
  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY}
        chat:
          options:
            model: gemini-3.8-flash
```

**Leçon :** lire la dernière ligne `Caused by` d'une trace, et ne pas dépendre du modèle par défaut d'un starter. La forme exacte de la propriété du modèle varie selon les pages de documentation : l'auto-complétion de l'IDE sur `spring.ai.google.genai.chat.` indique les clés valides de la version utilisée.

---

### Exercice 1.7 — Passer en local avec Ollama

**Objectif :** utiliser un modèle d'IA local avec Spring AI sans modifier le contrôleur.

#### Configuration utilisée

```yaml
spring:
  ai:
    model:
      chat: ollama
    ollama:
      chat:
        options:
          model: llama3.2:1b
```

#### Lancement

```bash
ollama pull llama3.2:1b
./mvnw spring-boot:run
```

#### Test

```text
http://localhost:8080/api/chat?message=Bonjour
```

#### Erreurs rencontrées

Aucune erreur précise liée à cet exercice n'a été consignée dans les informations disponibles. Aucune erreur n'est ajoutée artificiellement.

---

### Exercice 1.8 — Docker Model Runner

**Objectif :** lancer un modèle local avec Docker Model Runner et utiliser le starter OpenAI de Spring AI pour communiquer avec lui via une API compatible OpenAI.

#### Architecture utilisée

```text
Client HTTP
    |
    v
Spring Boot Application
    |
    v
Spring AI ChatClient
    |
    v
OpenAI Chat Model (client compatible OpenAI)
    |
    v
Docker Model Runner
    |
    v
Modèle local : smollm2
```

Docker Model Runner exécute le modèle localement et expose une API compatible avec le format OpenAI. Spring AI peut donc utiliser le starter OpenAI en redirigeant son client vers cette API locale.

#### Choix du modèle

Un premier `docker model run ai/gemma3` a lancé un téléchargement d'environ 7,3 Go (taille affichée par Docker pendant le téléchargement), qui a été interrompu. Le modèle léger `ai/smollm2` (quelques centaines de Mo) a été utilisé à la place. Sans précision d'étiquette, un nom de modèle peut pointer vers une variante beaucoup plus lourde que celle décrite dans le cours : il vaut mieux vérifier la taille réelle avant de télécharger.

La commande `docker model rm` lancée à ce moment a aussi déclenché le téléchargement de l'image `docker/model-runner` : c'est le moteur d'exécution des modèles et non un modèle.

#### Vérification de Docker avant Spring

```bash
docker model version
docker model list
curl http://localhost:12434/engines/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{"model":"ai/smollm2","messages":[{"role":"user","content":"Bonjour"}]}'
```

Le `curl` direct a renvoyé une réponse au format OpenAI (`choices[0].message.content`). Cela prouve que le port TCP 12434 est actif et que le chemin complet est `/engines/v1/chat/completions`.

#### Configuration utilisée

Dans `application.yml` :

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:dummy-key}
      base-url: http://localhost:12434/engines/v1
      chat:
        options:
          model: ai/smollm2
```

Le nom du modèle doit correspondre exactement à l'identifiant disponible dans Docker Model Runner.

**Remarque sur la clé :** avec la syntaxe `${OPENAI_API_KEY:dummy-key}`, si une vraie variable `OPENAI_API_KEY` existe sur la machine, c'est elle qui est utilisée et envoyée au serveur local. Une valeur factice fixe (`api-key: dummy-key`) évite ce cas.

#### Erreur rencontrée : HTTP 404

**Symptôme :** le premier appel effectué depuis Spring AI retournait une erreur HTTP 404.

**Cause identifiée :** le client OpenAI construit les endpoints à partir du `base-url` configuré. Dans l'environnement utilisé, l'API compatible OpenAI de Docker Model Runner était exposée sous le chemin `/engines/v1`. Un `base-url` ne contenant pas le bon chemin conduisait donc à une URL incorrecte.

**Précision apportée par la trace :** la trace montre que le starter OpenAI de Spring AI 2.0.1 s'appuie sur le client officiel d'OpenAI pour Java (classes `com.openai.*`, bibliothèque `openai-java-core`). Ce client attend que le `base-url` contienne déjà `/v1`. La valeur du cours (`http://localhost:12434/engines`, sans `/v1`) correspond à une version antérieure.

**Correction appliquée :**

```yaml
base-url: http://localhost:12434/engines/v1
```

**Méthode de diagnostic :**
1. Vérifier que Docker Model Runner est démarré.
2. Vérifier que le modèle attendu est disponible.
3. Tester l'endpoint de l'API locale avec `curl`.
4. Reporter le bon chemin dans `base-url`.
5. Relancer l'application Spring Boot et refaire l'appel.

Cette démarche permet de distinguer un problème d'accès à l'API locale d'un problème de configuration dans l'application Spring.

#### Pourquoi une clé API est-elle demandée avec un modèle local ?

Le starter OpenAI de Spring AI utilise un client compatible avec l'API OpenAI. Ce client attend une valeur pour le paramètre `api-key`, même lorsque son URL de base est redirigée vers un serveur local compatible.

Dans cet exercice, `dummy-key` est une valeur factice destinée à satisfaire la configuration du client. Ce n'est pas une véritable clé OpenAI.

Il faut distinguer trois éléments :

- **Configuration du client :** le client attend une valeur pour son paramètre `api-key`.
- **Exécution du modèle :** Docker Model Runner exécute le modèle sur la machine locale.
- **Authentification du serveur :** une clé factice ne remplace pas une authentification réelle si le serveur local en exige une.

#### Mesure observée avec `curl` (une seule requête)

Réponse de Docker Model Runner au `curl` direct avec `ai/smollm2` :

| Donnée | Valeur observée |
|---|---|
| Tokens du prompt | 32 (dont 26 déjà en cache) |
| Tokens générés | 32 |
| Vitesse de génération | environ 39 tokens par seconde |

Il s'agit d'un seul essai, après un premier appel du modèle : cette mesure ne permet pas de généraliser.

#### Comparaison effectuée : Gemini et `smollm2`

La même question a été posée à Gemini et au modèle local `smollm2`.

**Observation pendant les tests :** Gemini a pris un peu plus de temps pour répondre que le modèle léger exécuté localement avec Docker Model Runner. Dans cet environnement de test, `smollm2` a donc répondu plus rapidement.

Cette observation porte uniquement sur la vitesse constatée pendant ces essais. Elle ne permet pas de conclure que le modèle local fournit de meilleures réponses. La qualité doit être évaluée séparément avec les mêmes questions et des critères identiques.

| Critère | Gemini | Docker Model Runner (`smollm2`) |
|---|---|---|
| Hébergement | Cloud | Local |
| Connexion Internet | Nécessaire pour accéder à l'API | Pas nécessaire pour l'inférence locale |
| Temps de réponse observé | Un peu plus long pendant mes tests | Plus rapide pendant mes tests |
| Ressources d'exécution | Infrastructure du fournisseur | Ressources de la machine locale |
| Qualité des réponses | Non évaluée de manière chiffrée | Non évaluée de manière chiffrée |

---

## Exercices en cours (non validés)

### Exercice 1.11 — Deux modèles dans une application

**Objectif :** désactiver le `ChatClient.Builder` automatique, créer deux beans `ChatClient` (Gemini et le modèle local de Docker) et les injecter avec `@Qualifier`.

**Configuration mise en place :** les deux starters (Google GenAI et OpenAI) sont présents dans le classpath et `spring.ai.chat.client.enabled` est à `false`.

**Ce que le rapport de conditions confirme :** `ChatClientAutoConfiguration` n'est pas appliquée (la propriété a une autre valeur que `true`), et les auto-configurations de `GoogleGenAiChatModel` et `OpenAiChatModel` sont toutes deux appliquées.

**Erreur rencontrée au démarrage :**

```text
Parameter 0 of constructor in ...ChatController required a bean of type
'org.springframework.ai.chat.client.ChatClient$Builder' that could not be found.
```

**Cause :** l'ancien `ChatController` injecte un `ChatClient.Builder`, qui n'existe plus puisque son auto-configuration est désactivée. Spring arrête alors l'application avant de démarrer Tomcat, ce qui explique que `curl` ne pouvait pas se connecter au port 8080 (`Failed to connect`).

**Correction à appliquer (non encore confirmée) :** supprimer l'ancien `ChatController`, utiliser le contrôleur qui injecte les deux `ChatClient` avec `@Qualifier`, puis nettoyer le code compilé (`./mvnw clean` ou *Rebuild Project*), car une ancienne classe peut rester dans `target/classes` même après suppression du fichier source.

**Statut :** le résultat des deux endpoints n'a pas encore été consigné. L'exercice n'est donc pas marqué comme réalisé.

### Exercice 1.13 — Profils Spring

**Objectif :** choisir le fournisseur avec les profils `dev` et `prod`, sans aucun `if` dans le code Java.

**Problème en cours d'analyse :** dans le fichier de test, IntelliJ signale des symboles non résolus (`Test`, `Assertions`, `SpringBootTest`, `ActiveProfiles`). Quatre symboles de test manquent en même temps : la cause probable est l'absence de la bibliothèque de test dans le module (dépendance `spring-boot-starter-test`, rechargement Maven non fait, fichier hors de `src/test/java`, ou dépendances non téléchargées). La cause n'est pas encore confirmée.

**Statut :** non validé.

---

## Problèmes d'environnement rencontrés

| Contexte | Message ou symptôme | Cause identifiée | Action |
|---|---|---|---|
| Maven multi-modules | `Child module ...\exercise1-1 of ...\pom.xml does not exist` | Le parent déclarait un `<module>` dont le dossier n'existait pas à côté du `pom.xml` du parent | Placer le dossier du module dans le dossier du parent, avec exactement le nom déclaré dans `<module>` |
| IntelliJ | `Illegal char <:> at index ...` | Message d'IntelliJ qui interprétait mal la même erreur Maven et masquait la vraie cause | Lancer Maven en terminal (`./mvnw validate`) pour lire le vrai message |
| Git | `fatal: option '--cached' must come before non-option arguments` | Ordre des arguments de `git grep` | Écrire `git grep --cached -n "motif"` |
| GitHub | `Failed to connect to github.com:443 ... Could not connect to server` | Blocage ou indisponibilité du réseau, pas un problème d'authentification | Tester la connexion (`curl -I https://github.com`), vérifier le proxy, réessayer sur un autre réseau |
| Docker | Téléchargement de 7,3 Go lancé par `docker model run ai/gemma3` | Étiquette par défaut pointant vers une variante lourde | Interrompre, choisir un modèle léger dont la taille est vérifiée |

---

## Bilan des erreurs réellement documentées

| Exercice | Erreur | Cause | Correction |
|---|---|---|---|
| 1.1 | Aucune erreur au démarrage | Le projet lancé n'avait pas Spring AI dans son classpath | Vérifier le classpath et le projet réellement lancé |
| 1.3 | HTTP 500, `Failed to generate content` (404 de Google en cause racine) | Modèle Gemini par défaut retiré pour les nouveaux utilisateurs | Définir le modèle explicitement dans la configuration |
| 1.8 | HTTP 404 lors de l'appel à Docker Model Runner | Chemin incorrect ou incomplet dans le `base-url` (le client OpenAI officiel attend `/v1`) | Utiliser `http://localhost:12434/engines/v1` dans la configuration testée. |
| 1.11 | `ChatClient$Builder` introuvable au démarrage | Ancien contrôleur dépendant du builder désactivé | Supprimer l'ancien contrôleur et nettoyer le code compilé (à confirmer) |

Les autres exercices ne comportent pas d'erreur précise consignée dans les informations disponibles. Pour éviter d'inventer des incidents, seules les erreurs réellement signalées sont inscrites ici.

---

## Principaux acquis

- Utiliser `ChatClient` pour appeler un modèle depuis une application Spring Boot.
- Comprendre que la requête part lors de l'appel de la méthode finale (`content()`), pas lors de `prompt()`, `user()` ou `call()`.
- Comprendre la différence entre `ChatModel` + `Prompt` et `ChatClient`.
- Connecter Spring AI à un modèle local avec Ollama.
- Connecter Spring AI à Docker Model Runner via le starter OpenAI.
- Configurer un `base-url` pour cibler une API compatible OpenAI, et savoir que sa forme dépend de la version du client.
- Comprendre pourquoi le client peut demander une valeur `api-key`, même avec un modèle local.
- Lire la dernière ligne `Caused by` d'une trace pour trouver la vraie cause, et tester chaque maillon séparément (`curl` avant Spring).
- Ne jamais écrire une clé dans un fichier : variable d'environnement, `git grep --cached` avant commit, `.gitignore` adapté.
- Choisir un modèle explicitement plutôt que de dépendre du modèle par défaut d'un starter.
- Structurer un projet Maven parent avec des modules, et lire les erreurs de modules manquants.
- Isoler une expérience dans une branche Git dédiée.
- Distinguer la vitesse de réponse de la qualité des réponses.
