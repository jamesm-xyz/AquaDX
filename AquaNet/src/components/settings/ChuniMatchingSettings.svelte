<script lang="ts">
  import { CHU3_MATCHINGS } from "../../libs/config.js";
import { t } from "../../libs/i18n.js";
  import GameSettingFields from "./GameSettingFields.svelte";

  let custom = false;

</script>

<div class="matching">
  <h2>{t("userbox.header.matching")}</h2>
  <p class="notice">{t("settings.cabNotice")}</p>

  <div class="matching-selector">
    <button>{t('userbox.matching.select')}</button>
  </div>

  <div class="overlay">
    <div>
      <div>
        <h2>{t('userbox.header.matching')}</h2>
        <p>Choose the matching server you want to use.</p>
      </div>
      <div class="options">
        <!-- Selectable options -->
        {#each CHU3_MATCHINGS as option}
        <div class="option">
          <span class="name">{option.name}</span>
          <div class="links">
            <a href={option.ui} target="_blank" rel="noopener">{t('userbox.matching.option.ui')}</a> /
            <a href={option.guide} target="_blank" rel="noopener">{t('userbox.matching.option.guide')}</a>
          </div>

          <div class="divider"></div>

          <div class="coop">
            <span>Collaborators</span>
            <div>
              {#each option.coop as coop}
                <span>{coop}</span>
              {/each}
            </div>
          </div>
        </div>
        {/each}

        <!-- Placeholder option for "Custom" -->
        <div class="option">
          <span class="name">Custom</span>
          <p class="notice">Enter your own URL</p>
        </div>
      </div>
    </div>
  </div>

  {#if custom}
    <GameSettingFields game="chu3-matching"/>
  {/if}
</div>

<style lang="sass">
@use "../../vars"

.matching
  display: flex
  flex-direction: column
  gap: 12px

  h2
    margin-bottom: 0

  p.notice
    opacity: 0.6
    margin: 0

.options
  display: flex
  flex-wrap: wrap
  gap: 1rem

.option
  flex: 1
  display: flex
  flex-direction: column
  align-items: center

  border-radius: vars.$border-radius
  background: vars.$ov-light
  padding: 1rem
  min-width: 150px

  .divider
    width: 100%
    height: 0.5px
    background: white
    opacity: 0.2
    margin: 0.8rem 0

  .name
    font-size: 1.1rem
    font-weight: bold

  .coop
    text-align: center

    div
      display: flex
      flex-direction: column
      font-size: 0.9rem
      opacity: 0.6

</style>
