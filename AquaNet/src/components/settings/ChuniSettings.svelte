<!-- Svelte 4.2.11 -->

<script lang="ts">
  import {
    type AquaNetUser,
    type UserBox,
    type UserItem,
  } from "../../libs/generalTypes";
  import { DATA, USER, USERBOX } from "../../libs/sdk";
  import { t, ts } from "../../libs/i18n";
  import { DATA_HOST, FADE_IN, FADE_OUT, HAS_USERBOX_ASSETS } from "../../libs/config";
  import { fade, slide } from "svelte/transition";
  import StatusOverlays from "../StatusOverlays.svelte";
  import Icon from "@iconify/svelte";
  import GameSettingFields from "./GameSettingFields.svelte";
  import { filter } from "d3";
  import { coverNotFound } from "../../libs/ui";

  let user: AquaNetUser
  let [loading, error, submitting, preview] = [true, "", "", ""]
  let changed: string[] = [];

  // Available (unlocked) options for each kind of item
  // In allItems: 'namePlate', 'frame', 'trophy', 'mapIcon', 'systemVoice', 'avatarAccessory'
  let allItems: Record<string, Record<string, { name: string }>> = {}
  let iKinds = { namePlate: 1, frame: 2, trophy: 3, mapIcon: 8, systemVoice: 9, avatarAccessory: 11 }
  // In userbox: 'nameplateId', 'frameId', 'trophyId', 'mapIconId', 'voiceId', 'avatar{Wear/Head/Face/Skin/Item/Front/Back}'
  let userbox: UserBox
  let avatarKinds = ['Wear', 'Head', 'Face', 'Skin', 'Item', 'Front', 'Back']
  // iKey should match allItems keys, and ubKey should match userbox keys
  let userItems: { iKey: string, ubKey: keyof UserBox, items: UserItem[] }[] = []

  // Submit changes
  function submit(field: keyof UserBox) {
    let obj = { field, value: userbox[field] }
    if (submitting) return
    submitting = obj.field

    USERBOX.setUserBox(obj)
      .then(() => changed = changed.filter((c) => c !== obj.field))
      .catch(e => error = e.message)
      .finally(() => submitting = "")
  }

  // Fetch data from the server
  async function fetchData() {
    const profile = await USERBOX.getProfile().catch(_ => {
      loading = false
      error = t("userbox.error.nodata")
    })
    if (!profile) return
    userbox = profile.user
    userItems = Object.entries(iKinds).flatMap(([iKey, iKind]) => {
      if (iKey != 'avatarAccessory') {
        let ubKey = `${iKey}Id`
        if (ubKey == 'namePlateId') ubKey = 'nameplateId'
        if (ubKey == 'systemVoiceId') ubKey = 'voiceId'
        return [{ iKey, ubKey: ubKey as keyof UserBox,
          items: profile.items.filter(x => x.itemKind === iKind)
        }]
      }

      return avatarKinds.map((aKind, i) => {
        let items = profile.items.filter(x => x.itemKind === iKind && Math.floor(x.itemId / 100000) % 10 === i + 1)
        return { iKey, ubKey: `avatar${aKind}` as keyof UserBox, items }
      })
    })

    allItems = await DATA.allItems('chu3').catch(_ => {
      loading = false
      error = t("userbox.error.nodata")
    }) as typeof allItems

    console.log("User Items", userItems)
    console.log("All items", allItems)
    console.log("Userbox", userbox)

    loading = false
  }

  USER.me().then(u => {
    if (!u) throw new Error(t("userbox.error.nodata"))
    user = u
    return fetchData()
  }).catch((e) => { loading = false; error = e.message });
</script>

<StatusOverlays {error} loading={loading || !!submitting} />
{#if !loading && !error}
<div out:fade={FADE_OUT} in:fade={FADE_IN}>
  <h2>{t("userbox.header.general")}</h2>
  <GameSettingFields game="chu3"/>
  <h2>{t("userbox.header.userbox")}</h2>
  <div class="fields">
    {#each userItems as { iKey, ubKey, items }, i}
      <div class="field">
        <label for={ubKey}>{ts(`userbox.${ubKey}`)}</label>
        <div>
          <select bind:value={userbox[ubKey]} id={ubKey} on:change={() => changed = [...changed, ubKey]}>
            {#each items as option}
              <option value={option.itemId}>{allItems[iKey][option.itemId]?.name || `(unknown ${option.itemId})`}</option>
            {/each}
          </select>
          {#if changed.includes(ubKey)}
            <button transition:slide={{axis: "x"}} on:click={() => submit(ubKey)} disabled={!!submitting}>
              {t("settings.profile.save")}
            </button>
          {/if}
        </div>
      </div>
    {/each}
  </div>
  {#if HAS_USERBOX_ASSETS}
    <h2>{t("userbox.header.preview")}</h2>
    <p class="notice">{t("userbox.preview.notice")}</p>
    <input bind:value={preview} placeholder={t("userbox.preview.url")}/>
    {#if preview}
      <div class="preview">
        {#each userItems.filter(v => v.iKey != 'trophy' && v.iKey != 'systemVoice') as { iKey, ubKey, items }, i}
          <div>
            <span>{ts(`userbox.${ubKey}`)}</span>
            <img src={`${preview}/${iKey}/${userbox[ubKey].toString().padStart(8, '0')}.png`} alt="" on:error={coverNotFound} />
          </div>
        {/each}
      </div>
    {/if}
  {/if}
</div>
{/if}

<style lang="sass">
@use "../../vars"

input
  width: 100%

h2
  margin-bottom: 0.5rem

p.notice
  opacity: 0.6
  margin-top: 0

.preview
  margin-top: 32px
  display: flex
  flex-wrap: wrap
  justify-content: space-between
  gap: 32px

  > div
    position: relative
    width: 100px
    height: 100px
    overflow: hidden
    background: vars.$ov-lighter
    border-radius: vars.$border-radius

    span
      position: absolute
      bottom: 0
      width: 100%
      text-align: center
      z-index: 10
      background: rgba(0, 0, 0, 0.2)
      backdrop-filter: blur(2px)

    img
      position: absolute
      inset: 0
      width: 100%
      height: 100%
      object-fit: contain

.fields
  display: flex
  flex-direction: column
  gap: 12px
  width: 100%
  flex-grow: 0

  label
    display: flex
    flex-direction: column

  select
    width: 100%

.field
  display: flex
  flex-direction: column
  width: 100%

  label
    max-width: max-content

  > div:not(.bool)
    display: flex
    align-items: center
    gap: 1rem
    margin-top: 0.5rem

    > select
      flex: 1
</style>
