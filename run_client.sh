#!/bin/bash

command="java fr.univ_nantes.SimpleMahjong.Client.MahjongClientMain"
option=app-new-window
tilix --action=$option -e $command &

exit 0
