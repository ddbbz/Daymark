/**
 * 分页 shim。
 * 后端列表**无分页**（全量返回数组），这里做前端切片展示；
 * 后端补上 page/pageSize 后，把本 composable 的 slice 换成真实分页即可，调用方不变。
 */
import { computed, ref, type Ref } from 'vue'

export function usePaginationShim<T>(items: Ref<T[]>, pageSize = 20) {
  const visibleCount = ref(pageSize)

  const visible = computed(() => items.value.slice(0, visibleCount.value))
  const hasMore = computed(() => visibleCount.value < items.value.length)

  function loadMore() {
    visibleCount.value += pageSize
  }

  function reset() {
    visibleCount.value = pageSize
  }

  return { visible, hasMore, loadMore, reset }
}
