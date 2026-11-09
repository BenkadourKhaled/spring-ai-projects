# Spring AI Fast Track : journal d'exercices

Exercices du cours « Spring AI Fast Track », réalisés série par série.

**Stack technique :** Java 27, Spring Boot 4.1, Spring AI 2.0.1, Maven,
Docker Model Runner et Gemini.

## Avancement

  Série   Thème                                      Statut
  ------- ------------------------------------------ ------------------------------
  01      Say Hello to AI in Spring Boot             Exercices 1.1 à 1.8 terminés
  02      Prompts, Advisors et sorties structurées   À faire
  03      Fondations de l'IA générative              À faire
  04      Chat Memory                                À faire
  05      RAG                                        À faire
  06      Tool Calling                               À faire

## Prérequis

-   JDK installé et Maven (wrapper `mvnw` fourni).
-   Docker Desktop avec Docker Model Runner activé.
-   Un modèle local téléchargé et disponible dans Docker Model Runner.
-   Une clé API Gemini uniquement pour les tests avec Gemini.
-   Les secrets doivent être fournis par variables d'environnement,
    jamais enregistrés en clair dans le dépôt.

------------------------------------------------------------------------

## Série 01 : Say Hello to AI in Spring Boot

**Dossier :** `series-01/`

### Exercices réalisés

  -----------------------------------------------------------------------
  Exercice                            Résumé
  ----------------------------------- -----------------------------------
  1.1                                 Projet généré avec Spring
                                      Initializr

  1.3                                 Endpoint
                                      `GET /api/chat?message=...` avec
                                      `ChatClient`

  1.4                                 Beans Spring AI listés au démarrage
                                      (`BeanLister`), logs `debug=true`

  1.5                                 Estimation du coût de 10 000
                                      requêtes (courtes et longues)

  1.6                                 Comparaison `ChatModel` + `Prompt`
                                      contre `ChatClient`

  1.7                                 Passage du fournisseur cloud à
                                      Ollama, sans changer le contrôleur

  1.8                                 Intégration de Docker Model Runner
                                      avec le starter OpenAI,
                                      configuration du `base-url` et
                                      utilisation d'une clé factice
  -----------------------------------------------------------------------

### Exercice 1.7 --- Passer en local avec Ollama

**Objectif :** utiliser un modèle d'IA local avec Spring AI sans
modifier le contrôleur.

Configuration utilisée :

``` yaml
spring:
  ai:
    model:
      chat: ollama
    ollama:
      chat:
        options:
          model: llama3.2:1b
```

Lancement :

``` bash
ollama pull llama3.2:1b
./mvnw spring-boot:run
```

Test :

``` text
http://localhost:8080/api/chat?message=Bonjour
```

### Exercice 1.8 ★★ --- Docker Model Runner

**Objectif :** lancer un modèle avec Docker Model Runner et utiliser le
starter OpenAI de Spring AI pour communiquer avec ce modèle via une API
compatible OpenAI.

#### Architecture utilisée

``` text
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

Docker Model Runner exécute le modèle localement et expose une API
compatible avec le format OpenAI. Spring AI peut donc utiliser le
starter OpenAI en redirigeant son client vers cette API locale.

#### Configuration

Configuration utilisée dans `application.yml` :

``` yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:dummy-key}
      base-url: http://localhost:12434/engines/v1
      chat:
        options:
          model: ai/smollm2
```

Le nom du modèle doit correspondre exactement à l'identifiant exposé par
Docker Model Runner dans l'environnement utilisé.

La valeur `dummy-key` est une clé factice de configuration. Elle ne
constitue pas une véritable clé OpenAI.

#### Correction de l'erreur HTTP 404

**Problème rencontré :** le premier appel depuis Spring AI retournait
une erreur HTTP 404.

**Cause identifiée :** Spring AI 2.0.1 utilise le client officiel
OpenAI. Celui-ci construit les endpoints à partir du `base-url`
configuré. Docker Model Runner expose ici son API compatible OpenAI sous
le chemin `/engines/v1`. Un `base-url` incomplet ou incorrect conduit
donc le client à appeler une URL qui ne correspond pas à l'endpoint
attendu.

**Correction appliquée :**

``` yaml
base-url: http://localhost:12434/engines/v1
```

Cette configuration indique au client OpenAI d'utiliser le point
d'entrée API de Docker Model Runner.

**Méthode de diagnostic retenue :** tester d'abord l'endpoint avec
`curl`, puis effectuer le test depuis Spring Boot. Cela permet de
distinguer un problème d'accessibilité ou de configuration du serveur
local d'un problème dans l'intégration Spring AI.

#### Pourquoi une clé API est-elle demandée si le modèle est local ?

Le starter OpenAI de Spring AI s'appuie sur le client officiel OpenAI.
Ce client attend une valeur pour le paramètre `api-key`, même lorsque
son URL de base est redirigée vers un serveur compatible OpenAI hébergé
localement.

Dans cet exercice, la clé factice satisfait cette exigence de
configuration du client. Le modèle, lui, est exécuté par Docker Model
Runner sur la machine locale : il ne s'agit pas d'une requête envoyée
aux serveurs OpenAI.

Il faut donc distinguer :

-   **La configuration du client :** le client OpenAI attend une valeur
    pour son paramètre de clé API.
-   **L'exécution du modèle :** Docker Model Runner effectue l'inférence
    localement.
-   **L'authentification du serveur :** une clé factice ne remplace pas
    une authentification réelle si le serveur local en exige une.

#### Comparaison : Gemini et le modèle local `smollm2`

J'ai posé la même question à Gemini et au modèle local `smollm2` afin de
comparer leur comportement.

**Observation personnelle :** lors de mes tests, Gemini a pris un peu
plus de temps pour répondre que le modèle léger installé localement avec
Docker Model Runner. Le modèle local a donc été plus rapide dans mon
environnement de test.

Cette observation concerne la latence constatée pendant mes essais. Elle
ne signifie pas que le modèle local fournit nécessairement de meilleures
réponses. La qualité doit être évaluée séparément, avec les mêmes
questions et des critères identiques.

  -----------------------------------------------------------------------
  Critère                 Gemini                  Docker Model Runner
                                                  (`smollm2`)
  ----------------------- ----------------------- -----------------------
  Hébergement             Cloud                   Local

  Connexion Internet      Nécessaire pour l'API   Pas nécessaire pour
                                                  l'inférence locale

  Temps de réponse        Un peu plus long lors   Plus rapide lors de mes
  observé                 de mes tests            tests

  Ressources d'exécution  Infrastructure du       Ressources de la
                          fournisseur             machine locale

  Qualité des réponses    À évaluer avec les      À évaluer avec les
                          mêmes questions         mêmes questions
  -----------------------------------------------------------------------

Les durées exactes et les évaluations de qualité restent à renseigner
dans `mesures.md` : elles ne sont pas chiffrées ici.

#### Piste d'amélioration

Pour tester un modèle local différent, je peux ensuite essayer
`ai/gemma3:4B-Q4_K_M`, en ne modifiant que la propriété `model`, après
avoir vérifié que ce modèle est bien disponible dans Docker Model
Runner.

L'objectif est de comparer les modèles dans les mêmes conditions, sans
modifier le contrôleur ni l'architecture de l'application.

### Ce que j'ai appris

-   Docker Model Runner permet d'exécuter des modèles d'IA localement.
-   Spring AI peut utiliser le starter OpenAI pour communiquer avec un
    serveur tiers compatible avec l'API OpenAI.
-   Le `base-url` permet de rediriger le client vers le serveur
    compatible choisi.
-   Le client OpenAI peut exiger une valeur `api-key` même lorsque le
    serveur cible est local.
-   Une clé factice ne sert qu'à satisfaire la configuration du client
    lorsque le serveur local n'exige pas d'authentification.
-   Une erreur HTTP 404 peut provenir d'un chemin API incorrect dans le
    `base-url`.
-   Tester d'abord l'API avec `curl` facilite le diagnostic avant de
    tester l'intégration Spring Boot.
-   Un modèle local léger peut répondre plus rapidement qu'un modèle
    cloud dans un environnement donné.
-   La vitesse et la qualité des réponses sont deux critères distincts
    qui doivent être mesurés séparément.

### Erreurs rencontrées

  -------------------------------------------------------------------------------------
  Erreur                  Cause                   Correctif
  ----------------------- ----------------------- -------------------------------------
  `Non-parseable POM`     Commande `ollama pull`  Supprimer la ligne et exécuter la
                          collée dans le          commande dans le terminal
                          `pom.xml`               

  HTTP 404 avec Docker    `base-url` incorrect ou Utiliser
  Model Runner            chemin API incomplet    `http://localhost:12434/engines/v1`

  Configuration de la clé Le client OpenAI attend Fournir une clé factice si le serveur
  API                     une valeur `api-key`    local n'exige pas d'authentification
  -------------------------------------------------------------------------------------

------------------------------------------------------------------------

## Prochaines étapes

-   [ ] Compléter les mesures de comparaison Gemini / `smollm2` dans
    `mesures.md`.
-   [ ] Tester `ai/gemma3:4B-Q4_K_M` avec Docker Model Runner.
-   [ ] Commencer l'exercice 1.9 --- Bedrock : préparer AWS.
-   [ ] Continuer les exercices de la série 01 avant de passer à la
    série 02.

------------------------------------------------------------------------

## Modèle à copier pour les prochaines séries

## Série 0X : Nom de la série

**Dossier :** `series-0X/`

  Exercice   Résumé
  ---------- -------------
  X.1        À compléter

### Ce que j'ai appris

À compléter.

### Erreurs rencontrées

  Erreur        Cause         Correctif
  ------------- ------------- -------------
  À compléter   À compléter   À compléter
