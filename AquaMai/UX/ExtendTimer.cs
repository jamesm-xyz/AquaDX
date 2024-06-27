﻿using HarmonyLib;
using Manager;
using Monitor;
using Process;
using Process.Entry.State;
using Process.ModeSelect;

namespace AquaMai.UX
{
    public class ExtendTimer
    {
        [HarmonyPrefix]
        [HarmonyPatch(typeof(TimerController), "PrepareTimer")]
        public static void PrePrepareTimer(ref int second)
        {
            second = 65535;
        }

        [HarmonyPrefix]
        [HarmonyPatch(typeof(CommonTimer), "SetVisible")]
        public static void CommonTimerSetVisible(ref bool isVisible)
        {
            isVisible = false;
        }

        [HarmonyPrefix]
        [HarmonyPatch(typeof(EntryProcess), "DecrementTimerSecond")]
        public static bool EntryProcessDecrementTimerSecond(ContextEntry ____context)
        {
            SoundManager.PlaySE(Mai2.Mai2Cue.Cue.SE_SYS_SKIP, 0);
            ____context.SetState(StateType.DoneEntry);
            return false;
        }

        [HarmonyPrefix]
        [HarmonyPatch(typeof(ModeSelectProcess), "UpdateInput")]
        public static bool ModeSelectProcessUpdateInput(ModeSelectProcess __instance)
        {
            if (!InputManager.GetButtonDown(0, InputManager.ButtonSetting.Button05)) return true;
            __instance.TimeSkipButtonAnim(InputManager.ButtonSetting.Button05);
            SoundManager.PlaySE(Mai2.Mai2Cue.Cue.SE_SYS_SKIP, 0);
            Traverse.Create(__instance).Method("TimeUp").GetValue();
            return false;
        }

        [HarmonyPrefix]
        [HarmonyPatch(typeof(PhotoEditProcess), "MainMenuUpdate")]
        public static void PhotoEditProcess(PhotoEditMonitor[] ____monitors, PhotoEditProcess __instance)
        {
            if (!InputManager.GetButtonDown(0, InputManager.ButtonSetting.Button04)) return;
            SoundManager.PlaySE(Mai2.Mai2Cue.Cue.SE_SYS_SKIP, 0);
            ____monitors[0].SetButtonPressed(InputManager.ButtonSetting.Button04);
            Traverse.Create(__instance).Method("OnTimeUp").GetValue();
        }
    }
}