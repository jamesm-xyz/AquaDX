# Chunithm National Matching Guide

The national matching game mode allows up to 4 players on any server (YES, ANY SERVER) to play together.
In this game mode, for example, you can play with RinNET or Missless players as well.
This is a guide on how to set up your client for national matching.

This is tested on Chusan 2.27.

## Pre-requisites

- Play the normal game at least once so that you have a profile on the server.

## Setting Up

![](chu3-matching.png)

1. Go to the AquaNet website and set your matching server to "Yukiotoko"
2. Make sure you use [Dniel97's open-source segatools](https://gitea.tendokyu.moe/Dniel97/SEGAguide/wiki/SDHD)  
   If you're using fufubot segatools, please override it with Dniel97's version (don't forget to update `segatools.ini`).
3. Patch your `chusanApp.exe` using [Two-Torial's open-source patcher](https://patcher.two-torial.xyz/)  
   (Make sure you disable "Set all timer to 999", enable "No encryption", "No TLS", "Patch for head-to-head play")
4. Make sure you only have official options.  
   (Remove things like A999, AOMN, etc.)
5. Pet your cat 🐈
6. Launch!

## How to Play

When you enter the matching mode, it will assign to you a matching room if other people are online, or create a new room otherwise. 
Then, after four people are present or after a specific amount of time has passed, the game will start.
Everyone will be asked to pick a song at the start, even though your song might not be the first one to be played.
After songs are picked, other players will play the song on the SAME DIFFICULTY as what you picked.
(So be a nice person and don't pick 15 if there are new players alright? 🥺)

If there are less than 4 players when the timer runs out, the game will fill in the empty slots with bots.
The bots will randomly select a song (mostly under Lv10).
