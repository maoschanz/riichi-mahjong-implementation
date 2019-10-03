# Mahjong RMI

a simple Java RMI application where clients can play japanese mahjong ("riichi")

most rules concerning points and scores are not implemented, since they are boring and this is just some exercise to show i know about distributed algorithms

----

## How to install

- Dependencies:
	- `openjdk-11-jdk`
	- `tilix` or `gnome-terminal`
- Run `./run.sh full_run` to start 1 server and 4 clients on localhost

> `./run.sh help` to get informations about this amazingly pertinent build system

## How to play

Complete rules: [en](https://en.wikipedia.org/wiki/Japanese_Mahjong), [fr](https://fr.wikipedia.org/wiki/R%C3%A8gles_du_Mah-jong_%28Riichi%29)

[See also](http://arcturus.su/wiki/List_of_terminology_translations)

### Basic rules

- Tiles are arranged in a square wall, in the middle of it is the *river*.
- Each player is associated with a cardinal point, the first to play is East, then South, West, Nord, ...
- The goal of each player is to complete a valid hand:
	- [3 similar tiles OR a sequence of 3 following tiles] × 4 + [a pair]
	- [4 similar tiles] + [3 similar tiles OR a sequence of 3 following tiles] × 3 + [a pair]
- When it's their turn, a player draw a tile from the wall, and discard a tile in the river.
- When a tile is discarded, any player can interrupt the game by announcing:
	- *pon* and "steal" the last discarded tile to complete a set of 3 similar tiles
	- or *kan* to complete a set of 4 similar tiles
	- or *ron* to complete the entire hand and win
- When the opponent at your left discard a tile, it's possible to announce *chii* to "steal" it to complete a sequence of 3 tiles
- When the hand is completed by drawing in the wall, the victory is announced by *tsumo*
- When a valid hand is completed, the round ends and points are distributed from the loosers to the winner according to incredibly complex criteria. The next round can then start.

### Points

The composition of a winning hand, as well as the method used to complete it, will change how many points the winner get, and who will provide these points.

<!-- TODO -->

<!-- règles à implémenter au minimum : -->

<!-- - la distinction ron/tsumo -->
<!-- - les riichi -->

------

The readme is in english but the code and comments are plutôt en français.
