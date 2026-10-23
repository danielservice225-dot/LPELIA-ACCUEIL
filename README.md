# LPELIA Accueil

Assistant vocal d'accueil pour l'école : le visiteur parle (ou écrit), l'assistant répond et la réponse
est lue à voix haute sur les haut-parleurs de son téléphone, tablette ou ordinateur.

- `app.py` : backend Python (FastAPI). Garde les clés API côté serveur et envoie les réponses en streaming.
- Le fournisseur par défaut est Groq (llama-3.3-70b-versatile). Anthropic/Claude reste disponible en option.
- `static/index.html` : l'assistant pour les visiteurs.
- `static/admin.html` : la page d'entraînement, à l'adresse `/admin`.
- `knowledge.seed.json` : les connaissances de départ (voir « Sauvegarder l'entraînement »).

## 1. Obtenir une clé API

Créez une clé Groq sur https://console.groq.com/keys et ajoutez-la sur Render comme variable secrète `GROQ_API_KEY`.
La clé reste sur le serveur, jamais dans la page web. Par défaut, LPELIA Accueil utilise Groq (llama-3.3-70b-versatile).
Pour utiliser Claude à la place, réglez `AI_PROVIDER=anthropic` et ajoutez `ANTHROPIC_API_KEY` depuis https://console.anthropic.com.

## 2. Mettre en ligne (nécessaire pour le micro sur téléphone et tablette)

Les navigateurs n'autorisent le micro que sur une adresse **https**. Render la fournit automatiquement.

1. Mettez ce dossier sur GitHub (un dépôt, même privé).
2. Sur https://render.com : **New > Blueprint**, choisissez le dépôt. Render lit `render.yaml`.
3. Renseignez `GROQ_API_KEY` et `ADMIN_CODE` quand il les demande. Laissez `ACCESS_CODE` vide pour un accueil
   ouvert à tous. La variable `AI_PROVIDER` est réglée sur `groq` par défaut.
4. Ouvrez l'adresse `https://lpelia-accueil-xxxx.onrender.com`.

Le `Dockerfile` fonctionne aussi sur Railway, Fly.io, Google Cloud Run ou un VPS : définissez les mêmes variables.
Le plan gratuit de Render s'endort sans visite, la première question peut donc prendre une trentaine de secondes.

## 3. Entraîner LPELIA Accueil

Ouvrez `https://votre-adresse/admin` et entrez le `ADMIN_CODE`. Remplissez :

- **Identité** : nom de l'assistant, nom de l'école, message d'accueil.
- **Connaissances** : présentation de l'école, filières, inscriptions et frais, contacts et horaires.
- **Questions fréquentes** : questions avec la réponse exacte à donner. Elles passent avant tout le reste.
- **Réglages avancés** : boutons de questions proposées, ton souhaité.

Cliquez sur **Enregistrer** : la prochaine question utilise déjà les nouvelles informations.

À savoir sur le fonctionnement : ce n'est pas un entraînement du modèle. L'assistant relit vos informations à
chaque question, ce qui est plus simple et plus fiable pour des renseignements qui changent (frais, dates).
Il a pour consigne de ne jamais inventer un prix, une date ou une condition, et d'orienter vers l'école quand
l'information manque. Plus vos textes sont précis, meilleures sont les réponses. Limite : 80 000 caractères au total.

### Sauvegarder l'entraînement

Sur un hébergement gratuit, le disque est effacé à chaque redémarrage ou redéploiement. Donc :

1. Après chaque mise à jour importante, cliquez sur **Exporter** dans `/admin`.
2. Remplacez le fichier `knowledge.seed.json` de votre projet par celui téléchargé, puis poussez sur GitHub.

Au redémarrage, l'assistant recharge ce fichier. Si vous avez un hébergement avec disque persistant
(ex. disque Render payant monté sur `/app/data`), l'enregistrement y survit seul, mais gardez quand même une exportation.

## 4. Le son et le micro

- **Le son** sort par les haut-parleurs de l'appareil (ou le casque Bluetooth connecté). Le bouton **Tester le son**
  permet de vérifier. Si rien ne sort : montez le volume média, et sur iPhone désactivez le mode silencieux
  (interrupteur sur le côté).
- **Le micro** : au premier clic sur l'aura, le navigateur demande l'accès. Choisissez **Autoriser**.
  Si vous avez refusé : Chrome > cadenas à gauche de l'adresse > Autorisations > Micro > Autoriser.
  Safari (iPhone, iPad) > icône « aA » > Réglages du site web > Micro. Sur iPhone, la **Dictée** doit être activée
  (Réglages > Général > Clavier).

## 5. Variables d'environnement

| Variable | Rôle | Défaut |
|---|---|---|
| `GROQ_API_KEY` | Clé API Groq (obligatoire si `AI_PROVIDER=groq`) | n/a |
| `AI_PROVIDER` | Fournisseur IA : `groq` ou `anthropic` | `groq` |
| `GROQ_MODEL` | Modèle Groq | `llama-3.3-70b-versatile` |
| `ANTHROPIC_API_KEY` | Clé Claude (si `AI_PROVIDER=anthropic`) | n/a |
| `ADMIN_CODE` | Code de la page `/admin`. Sans lui, `/admin` est fermée | vide |
| `ACCESS_CODE` | Code demandé aux visiteurs. Vide = accueil ouvert à tous | vide |
| `MAX_DAILY_QUESTIONS` | Plafond de questions par jour pour tout le site | `500` |
| `RATE_LIMIT_PER_MIN` | Questions par minute et par adresse IP | `12` |
| `CLAUDE_MODEL` | Modèle utilisé | `claude-sonnet-5-5` |
| `MAX_TOKENS` | Longueur maximale d'une réponse | `600` |
| `DATA_DIR` | Dossier où sont enregistrées les connaissances | `./data` |

## 6. Tester sur votre ordinateur

```bash
python -m venv .venv
source .venv/bin/activate          # Windows : .venv\Scripts\activate
pip install -r requirements.txt
export ANTHROPIC_API_KEY=sk-ant-... ADMIN_CODE=mon-code   # Windows : set au lieu de export
uvicorn app:app --reload
```

Ouvrez http://localhost:8000 (le micro marche sur `localhost` sans https) et http://localhost:8000/admin.

## Limites à connaître

- **La dictée vient du navigateur** : Chrome, Edge et Safari. Firefox ne la propose pas ; sur Firefox, le visiteur écrit
  et la réponse est quand même lue à voix haute. La dictée de Chrome envoie l'audio aux serveurs de Google.
- Les voix dépendent de l'appareil. Sans voix française installée, la voix par défaut est utilisée.
- Chaque question coûte du crédit API : gardez `MAX_DAILY_QUESTIONS` et la limite par minute.
- Sans `ACCESS_CODE`, n'importe qui peut poser des questions. C'est voulu pour un accueil public, mais le plafond
  journalier est alors votre protection.
