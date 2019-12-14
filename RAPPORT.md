# Middleware - Rapport de projet

<!-- TODO mettre mon nom avant de faire un PDF, mais ne pas le push publiquement -->

>idées lumineuses, problèmes rencontrés, 2 pages

## Introduction : le jeu du Mahjong

D'origine chinoise, le mahjong est un jeu où 4 joueurs, identifiés par un point
cardinal, tentent de compléter des combinaisons de tuiles en piochant à tour de
rôle de nouvelles tuiles et en se défaussant de celles qui les intéressent peu.

Le mahjong s'est répandu au Japon au cours du XXe siècle, où ses règles ont
évolué pour donner une variante spécifiquement japonaise : le _riichi mahong_,
dont une version simplifiée est ici implémentée. En effet, une partie se déroule
généralement en plusieurs manches, où les mises augmentent petit à petit. Le
système de mises, de points, etc. n'étant pas implémenté, le projet se focalise
sur le déroulement d'une unique manche de jeu, simplifié ainsi :

- Chaque joueur se voit attribuer (au hasard) 13 tuiles, ainsi qu'un point
cardinal (on parlera de "vents"). Le vent d'est (symbolisé "東") commencera à
jouer en piochant une 14ème tuile.
- Le but est de former une main complète :
	- une paire
	- 4 combinaisons (suite de 3 tuiles numérotées, brelan, ou carré)
- Excepté le cas du début de manche, le joueur dont c'est le tour peut, au
choix :
	- Piocher parmi les tuiles non-distribuées. Au _riichi mahjong_, il existe
	en tout 136 tuiles, les tuiles non distribuées constituant la _muraille_.
	Afin de conserver le bon nombre de tuiles, le joueur ayant pioché doit alors
	se débarasser d'une de ses tuiles.
	- "Voler" la tuile dont le joueur précédent s'est défaussé. Cele se fait à
	condition que la tuile volée complète immédiatement une combinaison
	(suite/brelan/carré). Pour s'assurer du respect de cette règle, le joueur
	qui vole une tuile doit l'annoncer préalablement (respectivement en disant
	"chii, "pon" ou "kan"), et dévoiler la combinaison ainsi formée. Si la
	combinaison est un carré, le joueur doit piocher pour garder une main de la
	bonne taille.
	- Si la tuile obtenue complète entièrement la main et fait gagner le joueur,
	il faut aussi l'annoncer ("tsumo" si la tuile vient de la pioche ou "ron" si
	elle vient des tuiles défaussées par un adversaire).
- Les joueurs dont ce n'est **pas** le tour de jouer peuvent également à tout
moment interrompre le cours du jeu en volant eux aussi la tuile défaussée par le
dernier joueur. Ils ne peuvent cependant annoncer que "pon", "kan" ou "ron" (pas
"chii").

----

## Le serveur (`fr.univ_nantes.SimpleMahjong.Server`)

### Gestion des requêtes de connexion

Comme expliqué, le jeu exige exactement 4 joueurs. La première tâche du serveur
est donc de les mettre en relation : les clients qui demandent à jouer sont
envoyés vers un objet `MahjongLobby` qui attendra d'avoir 4 joueurs avant de les
envoyer vers une "table de jeu" (objet `MahjongTableManager`). Cela exige de
s'assurer de l'atomicité du comptage des joueurs demandant à se connecter, pour
s'assurer qu'ils seront réellement exactement 4. Une fois envoyés à une "table
de jeu", les joueurs vont interagir entre eux, et avec le `MahjongTableManager`,
mais n'auront plus besoin du `MahjongLobby` : celui-ci les oublie donc, et reste
disponible pour traiter les demandes de nouveaux clients qui voudraient jouer.

### Gestion d'une partie

Une fois qu'on a les 4 joueurs, on peut commencer à jouer une manche. Pour cela,
`MahjongTableManager` va initialiser un `ArrayList` de tuiles où les joueurs
pourront piocher, qu'on appellera la `muraille`. La méthode (côté client) pour
initialiser la main des 4 joueurs est alors appelée, et on attribue un vent à
chacun des 4 joueurs. Ils peuvent désormais commencer à jouer.

Durant la partie, le `MahjongTableManager` ne servira qu'à fournir la méthode
`pioche()` via son interface `MahjongTableInterface`.

Cette méthode donne au client qui l'invoque une tuile parmi la `muraille`, la
tuile est alors retirée de la `muraille`, s'assurant ainsi qu'on ne donnera pas
à un client une tuile déjà distribuée. Les joueurs peuvent ainsi prendre des
décisions de jeu fiables en fonction des tuiles de leur main et de celles déjà
défaussées (ils doivent pouvoir compter les tuiles pour estimer leurs chances de
compléter telle ou telle combinaison).

## Les clients (`fr.univ_nantes.SimpleMahjong.Client`)

..........................

parler du thread d'arrière-plan

## Code commun au serveur et aux clients

Pour permettre la communication entre le serveur et les clients, plusieurs
interfaces étendant `java.rmi.Remote` sont déclarées dans le dossier
`fr.univ_nantes.SimpleMahjong.Interface` :

- `MahjongLobbyInterface` correspond au `MahjongLobby` du serveur, qui traite les
demandes de connexion des clients voulant commencer une partie.
- `MahjongTableInterface` correspond au `MahjongTableManager` du serveur, qui
gère la pioche pour un ensemble de 4 clients/joueurs.
- `MahjongPlayerInterface` correspond au `MahjongPlayer` du client. Il s'agit
de l'interface nécessaire à la fois pour que le serveur (`MahjongTableManager`)
initialise correctement les clients, mais aussi de l'interface qui permet aux
clients de communiquer entre eux.

En plus de ces interfaces (qui existent par nécessité étant donné le
fonctionnement de Java RMI), le client et le serveur partagent le package
`fr.univ_nantes.SimpleMahjong.Tuile`, où sont implémentés des objets
représentant les divers types de tuiles.

----

## Utilisation

### Lancer le projet

<!-- !!!! FIXME la dépendance tilix !!! -->
```
./run.sh full_run
```

### Jouer

...........

----

## Conclusion

.............................

dire qu'on ne fait rien contre les byzantins

.........................

résistance aux pannes

