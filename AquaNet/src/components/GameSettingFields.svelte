<script lang="ts">
  import { slide } from "svelte/transition";
  import { SETTING } from "../libs/sdk";
  import type { GameOption } from "../libs/generalTypes";
  import { ts } from "../libs/i18n";
  import StatusOverlays from "./StatusOverlays.svelte";
  import InputWithButton from "./ui/InputWithButton.svelte";

  export let game: string;
  let gameFields: GameOption[] = []
  let submitting = ""
  let error: string;

  SETTING.get().then(s => {
    gameFields = s.filter(it => it.game === game)
  })

  async function submitGameOption(field: string, value: any) {
    if (submitting) return false
    submitting = field

    await SETTING.set(field, value).catch(e => error = e.message).finally(() => submitting = "")
    return true
  }
</script>

<div class="fields">
  {#each gameFields as field}
    <div class="field {field.type.toLowerCase()}">
      {#if field.type === "Boolean"}
        <input id={field.key} type="checkbox" bind:checked={field.value}
                on:change={() => submitGameOption(field.key, field.value)}/>
        <label for={field.key}>
          <span class="name">{ts(`settings.fields.${field.key}.name`)}</span>
          <span class="desc">{ts(`settings.fields.${field.key}.desc`)}</span>
        </label>
      {/if}
      {#if field.type === "String"}
        <label for={field.key}>
          <span class="name">{ts(`settings.fields.${field.key}.name`)}</span>
          <span class="desc">{ts(`settings.fields.${field.key}.desc`)}</span>
        </label>
        <InputWithButton bind:field={field} callback={() => submitGameOption(field.key, field.value)}/>
      {/if}
    </div>
  {/each}
</div>

<StatusOverlays {error} loading={!gameFields.length || !!submitting}/>

<style lang="sass">
  .fields
    display: flex
    flex-direction: column
    gap: 12px

  .field.string
    flex-direction: column
    align-items: flex-start
    gap: 0.5rem

  .field.boolean
    align-items: center
    gap: 1rem

  .field
    display: flex

    label
      display: flex
      flex-direction: column
      max-width: max-content

      .desc
        opacity: 0.6
</style>
