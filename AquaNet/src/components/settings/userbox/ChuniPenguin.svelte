<script lang="ts">
    import { DDS } from "../../../libs/userbox/dds"
    import { ddsDB } from "../../../libs/userbox/userbox"

    const DDSreader = new DDS(ddsDB);

    export var chuniWear = 1100001;
    export var chuniHead = 1200001;
    export var chuniFace = 1300001;
    export var chuniSkin = 1400001;
    export var chuniItem = 1500001;
    export var chuniFront = 1600001;
    export var chuniBack = 1700001;
    export var classPassthrough: string = ``
</script>
<div class="chuni-penguin {classPassthrough}">
    <div class="chuni-penguin-body">
        <!-- Body -->
        {#await DDSreader.getFileFromSheet(`avatarAccessory:${chuniSkin.toString().padStart(8, "0")}`, 0, 0, 256, 400, 0.75) then imageURL}
            <img class="chuni-penguin-skin" src={imageURL} alt="Body">
        {/await}

        <!-- Face -->
        {#await DDSreader.getFileFromSheet("surfboard:CHU_UI_Common_Avatar_face_00.dds", 0, 0, 225, 150, 0.75) then imageURL}
            <img class="chuni-penguin-eyes chuni-penguin-accessory" src={imageURL} alt="Eyes">
        {/await}
        {#await DDSreader.getFileFromSheet("surfboard:CHU_UI_Common_Avatar_body_00.dds", 86, 103, 96, 43, 0.75) then imageURL}
            <img class="chuni-penguin-beak chuni-penguin-accessory" src={imageURL} alt="Beak">
        {/await}
        
        <!-- Arms (surfboard) -->
        {#await DDSreader.getFileFromSheet("surfboard:CHU_UI_Common_Avatar_body_00.dds", 0, 0, 85, 160, 0.75) then imageURL}
            <img class="chuni-penguin-arm-left chuni-penguin-arm" src={imageURL} alt="Left Arm">
        {/await}
        {#await DDSreader.getFileFromSheet("surfboard:CHU_UI_Common_Avatar_body_00.dds", 0, 0, 85, 160, 0.75) then imageURL}
            <img class="chuni-penguin-arm-right chuni-penguin-arm" src={imageURL} alt="Right Arm">
        {/await}

        <!-- Wear -->
        {#await DDSreader.getFileScaled(`avatarAccessory:${chuniWear.toString().padStart(8, "0")}`, 0.75, `avatarAccessory:01100001`) then imageURL}
            <img class="chuni-penguin-wear chuni-penguin-accessory" src={imageURL} alt="Wear">
        {/await}

        <!-- Head -->
        {#await DDSreader.getFileScaled(`avatarAccessory:${chuniHead.toString().padStart(8, "0")}`, 0.75, `avatarAccessory:01200001`) then imageURL}
            <img class="chuni-penguin-head chuni-penguin-accessory" src={imageURL} alt="Head">
        {/await}
        {#if chuniHead == 1200001}
            <!-- If wearing original hat, add the feather and attachment -->
            {#await DDSreader.getFileFromSheet("surfboard:CHU_UI_Common_Avatar_body_00.dds", 104, 153, 57, 58, 0.75) then imageURL}
                <img class="chuni-penguin-head-2 chuni-penguin-accessory" src={imageURL} alt="Head2">
            {/await}
            {#await DDSreader.getFileFromSheet("surfboard:CHU_UI_Common_Avatar_body_00.dds", 5, 160, 100, 150, 0.75) then imageURL}
                <img class="chuni-penguin-head-3 chuni-penguin-accessory" src={imageURL} alt="Head3">
            {/await}
        {/if}

        <!-- Face (Accessory) -->
        {#await DDSreader.getFileScaled(`avatarAccessory:${chuniFace.toString().padStart(8, "0")}`, 0.75, `avatarAccessory:01300001`) then imageURL}
            <img class="chuni-penguin-face-accessory chuni-penguin-accessory" src={imageURL} alt="Face (Accessory)">
        {/await}

        <!-- Item -->
        {#await DDSreader.getFileScaled(`avatarAccessory:${chuniItem.toString().padStart(8, "0")}`, 0.75, `avatarAccessory:01500001`) then imageURL}
            <img class="chuni-penguin-item chuni-penguin-accessory" src={imageURL} alt="Item">
        {/await}

        <!-- Front -->
        {#await DDSreader.getFileScaled(`avatarAccessory:${chuniFront.toString().padStart(8, "0")}`, 0.75) then imageURL}
            <img class="chuni-penguin-front chuni-penguin-accessory" src={imageURL} alt="Front">
        {/await}
        
        <!-- Back -->
        {#await DDSreader.getFileScaled(`avatarAccessory:${chuniBack.toString().padStart(8, "0")}`, 0.75) then imageURL}
            <img class="chuni-penguin-back chuni-penguin-accessory" src={imageURL} alt="Back">
        {/await}
    </div>
    <div class="chuni-penguin-feet">
        <!-- Feet -->
        {#await DDSreader.getFileFromSheet(`avatarAccessory:${chuniSkin.toString().padStart(8, "0")}`, 0, 410, 167, 80, 0.75) then imageURL}
            <img src={imageURL} alt="Feet">
        {/await}
    </div>
</div>
<!-- Truly sorry for the horrors below -->
<style lang="sass">
    @keyframes chuniPenguinBodyBob
        0%
            transform: translate(-50%, 0%) translate(0%, -50%)
        50%
            transform: translate(-50%, 10px) translate(0%, -50%)
        100%
            transform: translate(-50%, 0%) translate(0%, -50%)
    @keyframes chuniPenguinArmLeft
        0%
            transform: translate(-50%, 0) rotate(-2deg)
        50%
            transform: translate(-50%, 0) rotate(2deg)
        100%
            transform: translate(-50%, 0) rotate(-2deg)
    @keyframes chuniPenguinArmRight
        0%
            transform: translate(-50%, 0) scaleX(-1) rotate(-2deg)
        50%
            transform: translate(-50%, 0) scaleX(-1) rotate(2deg)
        100%
            transform: translate(-50%, 0) scaleX(-1) rotate(-2deg)

    img
        -webkit-user-drag: none

    .chuni-penguin
        height: 512px
        aspect-ratio: 1/2
        position: relative

        .chuni-penguin-body, .chuni-penguin-feet
            transform: translate(-50%, -50%)
            position: absolute
            left: 50%
  
        .chuni-penguin-body
            top: 50%
            z-index: 1
            animation: chuniPenguinBodyBob 2s infinite cubic-bezier(0.45, 0, 0.55, 1)
        .chuni-penguin-feet
            top: 82.5%
            z-index: 0

        .chuni-penguin-arm
            transform-origin: 95% 10%
            position: absolute
            top: 40%
        .chuni-penguin-arm-left
            left: 0%
            animation: chuniPenguinArmLeft 1.5s infinite cubic-bezier(0.45, 0, 0.55, 1)
        .chuni-penguin-arm-right
            left: 70%
            animation: chuniPenguinArmRight 1.5s infinite cubic-bezier(0.45, 0, 0.55, 1)

        .chuni-penguin-accessory
            transform: translate(-50%, -50%)
            position: absolute
            top: 50%
            left: 50%

        .chuni-penguin-eyes
            top: 22.5%
        .chuni-penguin-beak
            top: 29.5%
        .chuni-penguin-wear
            top: 57.5%
        .chuni-penguin-head
            top: 7.5%
            z-index: 10
        .chuni-penguin-head-2
            top: 12.5%
        .chuni-penguin-head-3
            top: -12.5%
        .chuni-penguin-face-accessory
            top: 27.5%
        .chuni-penguin-back
            z-index: -1
        
</style>