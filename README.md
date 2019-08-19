# bpmn-games

The games are modeled with BPMN and can be executed with Zeebe. 

Available games:

* [Exploding Kittens](https://github.com/saig0/bpmn-casino/blob/master/src/main/resources/explodingKittens.bpmn) (current only computer players)


## Install

> Requirements: 
>   * Java 11
>   * Zeebe 0.21.0-SNAPSHOT

1. Run `Application.java` to deploy the workflows and start the workers

2. Create a new workflow instance of workflow `exploding-kittens` with a variable `playerNames` (e.g. `["player1","player2"]`)
