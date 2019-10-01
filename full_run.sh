#!/bin/bash

echo "début de la compilation"

./compile.sh

echo "compilation terminée"

./run_server.sh

echo "serveur lancé"

sleep 2

echo "lancement des clients"

./run_client.sh
./run_client.sh
./run_client.sh
./run_client.sh
./run_client.sh

exit 0
