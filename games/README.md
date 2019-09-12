# bpmn-games

The games are modeled with BPMN and can be executed with Zeebe. 

Available games:

* [Exploding Kittens](https://github.com/saig0/bpmn-games/blob/master/games/src/main/resources/explodingKittens.bpmn)

## Install

> Requirements: 
>   * Java 11
>   * Zeebe >= 0.21.0-alpha2

1. Run `StandaloneApplication.java` to deploy the workflows and start the workers

2. Create a new workflow instance of workflow `exploding-kittens` with a variable `playerNames` (e.g. `["player1","player2"]`)
