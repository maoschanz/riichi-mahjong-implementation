#!/bin/bash

command="java fr.univ_nantes.SimpleMahjong.Client.MainMahjongClient"
option=app-new-window
tilix --action=$option -e $command &
# $command &

exit 0
