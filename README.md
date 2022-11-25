# 💥 exploding-bpmn 💥

Exploding BPMN is like the game _Exploding Kittens_ but with BPMN symbols. The game is modeled in
BPMN and executed in Camunda 8 SaaS.

## 💣 Usage

The Slack application provides two commands:

* `/how-to-play-exploding-bpmn` - prints a basic manual 
* `/exploding-bpmn` - creates a new game
  * provide the player names as argument (e.g. `/exploding-bpmn @me @player2 @player3`) 
  * use a player name like `bot_*` to add a bot (e.g. `/exploding-bpmn @me bot_1 bot_2`)
  * the game is played in private chats with the app

## 🚀 Install

### Set up a Slack App

1. Creating a new Slack App: https://api.slack.com/apps
2. Activate Incoming Webhooks
3. Enable Interactive Components
  * Setting request url to `${BASE_URL}/slack/action`
4. Create Slash Commands
  * New command `/exploding-bpmn` with request url `${BASE_URL}/slack/command/new-game`
  * New command `/how-to-play-exploding-bpmn` with request url `${BASE_URL}/slack/command/how-to-play`
5. Configure the Scopes (i.e. permissions) under `OAuth & Permissions`
  * `chat:write`
  * `chat:write.public`
  * `im:write`
  * `commands`
  * `incoming_webhook`
6. Store the OAuth Token for Your Workspace from `OAuth & Permissions`
7. Installing the app to the workspace

### Publish the Docker image 

The docker image is built and pushed using Jib. 

```
mvn clean deploy
```

### Deploy to K8s

```
# check the k8s cluster
kubectx -c

# create a new namespace
kubectl create namespace exploding-bpmn

# switch to namespace
kubens exploding-bpmn

# deploy resource definition
cd k8s
kubectl apply -f exploding-bpmn-app-secret.yaml
kubectl apply -f exploding-bpmn-app.yml

# check the status
kubectl get pods

# check the logs 
kubectl logs exploding-bpmn-app

# get the public IP address
kubectl get services
```

### Final Steps

1. Open the Slack app 
2. Set the public IP address for Interactivity Request URL
3. Set the public IP address for Slash commands

## 🔥 Development

1. Download and run [ngrok](https://dashboard.ngrok.com)
2. Setting request URLs to `ngrok.io` 
3. Start the application
4. Start a new game by creating a new instance of `exploding-kittens` 
  * with a variable `playerNames` (e.g. `["player1","player2"]`)

## 🔧 Configuration

Environment variables:

* `SLACK_TOKEN`
* `ZEEBE_CLIENT_CLOUD_CLUSTERID`
* `ZEEBE_CLIENT_CLOUD_CLIENTID`
* `ZEEBE_CLIENT_CLOUD_CLIENTSECRET`
* `ZEEBE_CLIENT_CLOUD_BASEURL` (default: `zeebe.ultrawombat.com`)
* `ZEEBE_CLIENT_CLOUD_REGION` (default: `bru-3`)
* `ZEEBE_CLIENT_CLOUD_AUTHURL` (default: `https://login.cloud.ultrawombat.com/oauth/token`)
   
