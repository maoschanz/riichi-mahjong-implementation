#!/bin/bash

# moi être débile 🤡🤡 maven être trop compliqué pour moi 🤪🤪🤡

TERMINAL_COMMAND="tilix --action=app-new-window -e"
# TERMINAL_COMMAND="gnome-terminal --"

mahjong_script_help() {
	echo "full_run		compile the project, start 1 server and 4 clients"
	echo "compile		compile all .java files into .class files"
	echo "clean		delete all .class files"
	echo "server		start a server"
	echo "client		start a client"
	echo "help		display this help"
	echo "--no-term		(as a 2nd argument) do not run the client in distinct terminal windows"
}

mahjong_run_client() {
	command="java fr.univ_nantes.SimpleMahjong.Client.MainMahjongClient"
	$TERMINAL_COMMAND $command &
	# $command &
}

mahjong_run_server() {
	command="java fr.univ_nantes.SimpleMahjong.Server.MainMahjongServer"
	# $TERMINAL_COMMAND $command &
	$command &
}

mahjong_clean() {
	rm fr/univ_nantes/SimpleMahjong/*/*.class
}

mahjong_compile() {
	javac fr/univ_nantes/SimpleMahjong/Interface/*.java
	javac fr/univ_nantes/SimpleMahjong/Server/*.java
	javac fr/univ_nantes/SimpleMahjong/Client/*.java
}

mahjong_full_run() {
	echo "début de la compilation"
	mahjong_compile
	echo "compilation terminée"
	mahjong_run_server
	echo "serveur lancé"
	sleep 2
	echo "lancement des clients"
	mahjong_run_client
	mahjong_run_client
	mahjong_run_client
	mahjong_run_client
	# mahjong_run_client
}

################################################################################

arg=$1

if [[ $2 = "--no-term" ]]; then
	TERMINAL_COMMAND=""
fi

if [[ $arg = "help" ]]; then
	mahjong_script_help
elif [[ $arg = "clean" ]]; then
	mahjong_clean
elif [[ $arg = "compile" ]]; then
	mahjong_compile
elif [[ $arg = "client" ]]; then
	mahjong_run_client
elif [[ $arg = "server" ]]; then
	mahjong_run_server
elif [[ $arg = "full_run" ]]; then
	mahjong_full_run
else
	echo "Argument inconnu."
	echo "Pour plus d'informations, essayez :"
	echo "./run.sh help"
fi

exit 0
