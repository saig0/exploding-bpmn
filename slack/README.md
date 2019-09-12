
## Slack Integration

1. Creating a new Slack App: https://api.slack.com/apps
2. Create BOT user
3. Installing App to the workspace
4. Store Bot Access Token and Signing Secret
5. Activate Incoming Webhooks
6. Enable Interactive Components
    * Setting request url to `${BASE_URL}/slack/action`
7. Enable Slash Commands 
    * New command `/new-game` with request url `${BASE_URL}/slack/command/new-game`
    * New command `/how-to-play` with request url `${BASE_URL}/slack/command/how-to-play`
    
    
## Development

1. Download and run [ngrok](https://dashboard.ngrok.com)
2. Setting request urls to `ngrok.io` url
3. Run `SlackApplication` class with activated Spring profile `local`
