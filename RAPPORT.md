# Middleware - Rapport de projet

Romain T*****, M2 ALMA, 2019-2020

<https://github.com/maoschanz/riichi-mahjong-implementation>

## Introduction : le jeu du Mahjong

D'origine chinoise, le mahjong est un jeu où 4 joueurs, identifiés chacun par un
point cardinal, tentent de compléter des combinaisons de tuiles en piochant à
tour de rôle de nouvelles tuiles et en se défaussant de celles qui les
intéressent le moins.

Le mahjong s'est répandu au Japon au cours du XXe siècle, où ses règles ont
évolué pour donner une variante spécifiquement japonaise : le _riichi mahjong_,
dont une version simplifiée est ici implémentée. En effet, une partie se déroule
généralement en plusieurs manches, où les mises augmentent petit à petit. Le
système de mises, de points, etc. n'étant pas implémenté, le projet se focalise
sur le déroulement d'une unique manche de jeu, simplifié ainsi :

- Chaque joueur se voit attribuer (au hasard) 13 tuiles, ainsi qu'un point
cardinal (on parlera de "vents"). Le vent d'est (symbolisé "東") commencera à
jouer en piochant une 14ème tuile.
- Le but est de former une main complète, composée de :
	- 1 paire
	- 4 combinaisons (suite de 3 tuiles numérotées, brelan, ou carré)
- Excepté certains cas tel que le début de manche, le joueur dont c'est le tour
peut, au choix :
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
- Si la tuile obtenue complète entièrement la main et fait gagner le joueur, il
faut aussi l'annoncer ("tsumo" si la tuile vient de la pioche ou "ron" si elle
vient des tuiles défaussées par un adversaire).
- Les joueurs jouent ainsi à tour de rôle dans l'ordre est/nord/ouest/sud.
- Les joueurs dont ce n'est **pas** le tour de jouer peuvent également à tout
moment interrompre le cours du jeu en volant eux aussi la tuile défaussée par le
dernier joueur. Ils ne peuvent cependant annoncer que "pon", "kan" ou "ron" (pas
"chii").

Étant donné la complexité des règles, et le fait que le jeu soit assez méconnu
en occident, personne n'était motivé à me suivre dans cette idée, et j'ai donc
réalisé ce projet seul. Par manque de temps à y consacrer, toutes les règles ne
sont pas implémentées, et le jeu n'aura pas d'interface graphique, on jouera
donc dans un terminal.

----

## Utilisation

Le jeu suit une architecture client-serveur sur certains aspects, et pair-à-pair
sur certains autres.

### Compiler le projet

Le code a été compilé et testé avec `openjdk 11` et le compilateur `javac` de
même version.

```
javac fr/univ_nantes/SimpleMahjong/Tuile/*.java
javac fr/univ_nantes/SimpleMahjong/Interface/*.java
javac fr/univ_nantes/SimpleMahjong/Server/*.java
javac fr/univ_nantes/SimpleMahjong/Client/*.java
```

### Lancer le projet

On peut ensuite lancer le serveur avec la commande

```
java fr.univ_nantes.SimpleMahjong.Server.MainMahjongServer
```

Puis, dans des terminaux distincts, les clients se lancent avec la commande

```
java fr.univ_nantes.SimpleMahjong.Client.MainMahjongClient
```

Il faut lancer un serveur et minimum 4 clients pour pouvoir jouer une partie.

### Jouer

Chaque joueur dispose d'un terminal, et doit entrer au clavier ce qui est
attendu de lui (cf. les instructions visibles dans l'interface).

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

N'ayant pas de quoi tester une autre configuration, il est hardcodé que les
demandes de connexion se font sur `rmi://localhost:8090/lobby`.

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

Dans l'architecture mise en œuvre, les clients correspondent aux joueurs.

Suite à son interaction initiale avec le serveur (`MahjongLobby`), le client se
voit attribuer une table de jeu (`MahjongTableManager`). Cette table présente au
joueur ses 3 adversaires, dont il conserve alors une référence pour les
contacter ultérieurement pendant le jeu. Le `MahjongTableManager` initialise
aussi la main du joueur (13 tuiles) et lui indique son "vent". Le dernier joueur
à être ainsi initialisé est le vent d'est, dont le client pioche alors
automatiquement une 14ème tuile : le jeu peut commencer.

L'utilisateur va interagir avec l'application via le terminal, où il tapera des
numéros correspondant à l'action qu'il souhaite effectuer. Ce fonctionnement m'a
ceci dit vite posé problème lors de mes premières tentatives d'implémentation :
récupérer les entrées au clavier avec `Scanner` bloque en effet l'exécution,
sans possibilité que ce blocage soit levé comme on le ferait avec les classiques
wait/notify. Cela empêche les autres joueurs d'interrompre le jeu pour voler une
tuile par exemple.

La solution pour laquelle j'ai opté fut de lancer un thread en arrière-plan de
chaque client, nommé `MahjongBackground`. Ce thread va demander en boucle une
entrée au clavier, et dès qu'il en a une il l'envoie à l'objet `MahjongPlayer`
où la chaîne de caractère entrée au clavier sera traitée.

Si **ce n'est pas** au tour du joueur ayant fourni cette entrée au clavier, la
chaîne sera traitée par `MahjongPlayer` comme une annonce préalable au vol d'une
tuile pour former une combinaison. Si l'annonce est valide ("pon", "kan", et
"ron" sont les seules possibilités), le jeu est alors interrompu et le joueur
ayant émis l'annonce révèle la combinaison qu'il vient de compléter. Le joueur
courant devient alors celui à sa droite (en termes de vents).

Quand le joueur fournit une entrée clavier à `MahjongBackground` alors que
**c'est effectivement son tour** de jouer, le contenu de ce qui a été tapé n'est
pas traité, l'entrée servant simplement à notifier le `MahjongPlayer` qu'il peut
lancer les scanners de demande des "vraies" entrées au clavier, qui ont elles
réellement besoin d'être bloquantes (explications plus loin) et ne vont ainsi
pas dépendre du thread d'arrière-plan : les étapes du jeu sont alors traitées
séquentiellement, jusqu'au moment où on notifie aux autres joueurs qu'on a
terminé. Cette information déclenche chez les autres joueurs une actualisation
de leur état et de leur interface utilisateur. Notamment, la dernière tuile
défaussée et l'identité du joueur actuel sont mises à jour.

Le jeu se poursuit ainsi, en transmettant les informations de joueurs à joueurs,
selon une architecture pair-à-pair. Le serveur n'est contacté que pour piocher
de nouvelles tuiles.

Pour des raisons pratiques (difficultés à tester le code), le code pour détecter
la victoire d'un joueur — via la pioche (_tsumo_) ou via le vol d'une tuile à un
adversaire (_ron_) — n'a pas eu le temps d'être implémenté. De même, les cas où
la partie est nulle (notamment quand la pioche est vide) ne sont pas pris en
charge.

Mais parmi les fonctionnalités réalisées, on a déjà bien assez de méthodes
concernées par les problématiques de l'algorithmique distribuée : l'interruption
du jeu par les annonces de vol de tuile a déjà été évoquée sous l'angle du
problème des entrées au clavier, mais les règles du jeu imposent aussi leurs
contraintes sur le déroulement de ces interruptions. En effet, il faut toujours
que chaque joueur garde un nombre de tuile ayant du sens : si l'annonce survient
— par exemple — entre le moment où le joueur a pioché et le moment où il se
défausse, l'annonce doit être rejetée pour que le joueur ait se défausse d'une
tuile et conserve une main valide. Ce verrouillage est réalisé avec l'attribut
booléen `canBeInterrupted`.

## Code commun au serveur et aux clients

Pour permettre la communication entre le serveur et les clients, plusieurs
interfaces étendant `java.rmi.Remote` sont déclarées dans le dossier
`fr.univ_nantes.SimpleMahjong.Interface` :

- `MahjongLobbyInterface` correspond au `MahjongLobby` du serveur, qui traite
les demandes de connexion des clients voulant commencer une partie.
- `MahjongTableInterface` correspond au `MahjongTableManager` du serveur, qui
gère la pioche pour un ensemble de 4 clients/joueurs.
- `MahjongPlayerInterface` correspond au `MahjongPlayer` du client. Il s'agit
de l'interface nécessaire pour que le serveur (`MahjongTableManager`) initialise
correctement les clients, mais aussi de l'interface qui permet aux clients de
communiquer entre eux.

En plus de ces interfaces (qui existent par nécessité étant donné le
fonctionnement de Java RMI), le client et le serveur partagent aussi le package
`fr.univ_nantes.SimpleMahjong.Tuile`, où sont implémentés des objets
représentant les divers types de tuiles. Chaque modèle de tuile a un identifiant
(qui permet d'afficher les tuiles triées et d'en détecter les combinaisons
notamment), un nom complet, et une représentation courte sous forme d'un ou deux
caractères à afficher dans l'interface du terminal.

----

## Conclusion

Parmi les améliorations qu'on pourrait apporter — outre une implémentation plus
complète des règles, y compris les fins de partie possibles — on peut citer
l'encapsulation de la main du joueur dans une classe dédiée, ce qui améliorerait
considérablement la lisibilité du code de `MahjongPlayer` mais que je n'avais
pas anticipé lors de la conception initiale.

Aussi, il serait pertinent que le jeu soit moins sensible aux byzantins : un
joueur peut pour l'instant utiliser une implémentation frauduleuse de
`MahjongPlayerInterface` pour tricher. Vérifier efficacement l'absence de triche
sans se reposer intégralement sur le serveur pour toutes les communications
impliquerait que les joueurs communiquent le contenu de leur main aux autres,
ce qui est impensable (ça ouvre la voie à d'autres tricheries). Comme dans une
partie réelle, on ne saura donc si quelqu'un triche que lorsqu'il révèlera des
tuiles qu'il n'est pas supposé avoir.

Pour limiter les soucis, une amélioration possible serait que les clients ne
puissent pas utiliser les méthodes dédiées à la communication du serveur vers le
client. Ça impliquerait que `MahjongPlayer` implémente simultanément deux
interfaces étendant `Remote`, ce que je n'ai pas réussi à faire fonctionner
correctement.

La résistance aux pannes est également problématique : il serait utile de mieux
prendre en compte le cas où un joueur se déconnecte. Par exemple, fournir un
moyen de quitter la partie proprement et permettre à quelqu'un d'autre de
récupérer la place laissée vacante pour que la partie puisse continuer.

Le projet est en attendant assez fonctionnel pour proposer la plupart des
interactions nécessaires à une manche de mahjong japonais, en utilisant les
fonctionnalités proposées par Java RMI.

