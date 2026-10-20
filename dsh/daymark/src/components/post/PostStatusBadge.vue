<script setup lang="ts">
import BaseBadge from '@/components/base/BaseBadge.vue'
import { postStatusMeta } from '@/utils/format'
import type { PostStatusValue } from '@/api/types/enums'

const props = defineProps<{ status: PostStatusValue }>()
</script>

<template>
  <BaseBadge :tone="postStatusMeta[props.status].tone">{{ postStatusMeta[props.status].label }}</BaseBadge>
</template>
