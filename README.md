# Mahjong RMI

a simple Java RMI application where clients can play japanese mahjong ("riichi")

most rules concerning points and scores are not implemented, since they are boring and this is just some exercise to show i know about distributed algorithms

----

## How to install

- Dependencies:
	- `openjdk-11-jdk`
	- `tilix` or `gnome-terminal`
- Run `./run.sh full_run` to start 1 server and 4 clients on localhost

## How to play

- Tiles are arranged in a wall, in the middle of it is the *river*.
- Each player is associated with a cardinal point, the first to play is East, then South, West, Nord, ...
- The goal of each player is to complete a valid hand:
	- [3 similar tiles OR a sequence of 3 following tiles] × 4 + [a pair]
	- [4 similar tiles] + [3 similar tiles OR a sequence of 3 following tiles] × 3 + [a pair]
- When it's their turn, a player draw a tile from the wall, and discard a tile in the river.
- When a tile is discarded, any player can interrupt the game by announcing:
	- *pon* and "steal" the discarded tile to complete a set of 3 similar tiles
	- or *kan* to complete a set of 4 similar tiles
	- or *ron* to complete the entire hand and win
- When the opponent at someone's left discard a tile, it's possible to announce *chii* to "steal" it to complete a sequence of 3 tiles
- When the hand is completed by drawing in the wall, the victory is announced by *tsumo*

------

The readme is in english but the code and comments are plutôt en français.
