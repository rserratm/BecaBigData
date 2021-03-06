#! /bin/bash

. environment.sh

REMOVE_WORD=0
REMOVE_FROM_LIST=1
REMOVE_A_LIST=2

function remove()
{
	REMOVE_TYPE=$1
	DICTIONARY=$2
	WORD=$3
	LIST=$4
	DICTIONARY_PATH=$DICTIONARIES_PATH/${DICTIONARY}$DICTIONARY_SUFFIX_NAME
	BACKUP_DICTIONARY_PATH=$DICTIONARIES_PATH/${DICTIONARY}${DICTIONARY_BACKUP_SUFFIX}$DICTIONARY_SUFFIX_NAME
	
	# Backing up dictionary.
	cp $DICTIONARY_PATH $BACKUP_DICTIONARY_PATH

	# Inserting mode.
	if [ $REMOVE_TYPE -eq $REMOVE_WORD ]
	then
		remove_word
	elif  [ $REMOVE_TYPE -eq $REMOVE_FROM_LIST ]
	then
		remove_from_list
	elif  [ $REMOVE_TYPE -eq $REMOVE_A_LIST ]
	then
		LIST=$WORD
		remove_list
	fi
	result=$?

	# Restoring if operation has failed.
	if [ $result -ne 0 ]
	then
		mv $BACKUP_DICTIONARY_PATH $DICTIONARY_PATH	
	fi 
}


# Removes the word from the dictionary
# In fact, removes the line where appears the word
function remove_word()
{	

	LINE=`cat $DICTIONARY_PATH  |  grep -n -w $WORD | awk -F':' '{ print $1 }' `
	
	# If the word exists, then remove it.
	# Without this shield, in case that the word didn't exist, then it would remove all the dictionary :(
	if [ "$LINE" != "" ]
	then
		sed -i ${LINE}d $DICTIONARY_PATH
	fi
}

# Removes the word from the list from the dictionary 
function remove_from_list()
{
	# Removing from the lines that start with $LIST= the word $WORD
	sed -i  "/^"${LIST}"=/ s/\(,*\)"$WORD"//" $DICTIONARY_PATH

	# Removing possible commas in case of deleting first word of the list
	sed -i  "/^"${LIST}"=/ s/=,/=/" $DICTIONARY_PATH

	WORD_LIST=`cat $DICTIONARY_PATH | grep ${LIST}= | awk -F'=' '{print $2}'`
	# if no elements in the list, removes it
	if [ "$WORD_LIST" == "" ]
	then
		remove_list
	fi
}

# Removes the list from the dictionary
function remove_list()
{
	LINE=`cat $DICTIONARY_PATH   | awk -F'=' '{print $1}' |  grep -n -w $LIST | awk -F':' '{ print $1 }' `
	
	# If the word exists, then remove it.
	# Without this shield, in case that the word didn't exist, then it would remove all the dictionary :(
	if [ "$LINE" != "" ]
	then
		sed -i ${LINE}d $DICTIONARY_PATH
	fi
}
