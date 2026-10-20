import { defineStore } from 'pinia'

export type ToastType = 'info' | 'success' | 'error'

export interface ToastItem {
  id: number
  message: string
  type: ToastType
}

let nextId = 0
const timers = new Map<number, ReturnType<typeof setTimeout>>()

export const useToastStore = defineStore('toast', {
  state: () => ({
    items: [] as ToastItem[],
  }),
  actions: {
    show(message: string, type: ToastType = 'info', duration = 3000): number {
      const id = nextId++
      this.items.push({ id, message, type })
      const timer = setTimeout(() => this.remove(id), duration)
      timers.set(id, timer)
      return id
    },
    success(message: string, duration?: number) {
      return this.show(message, 'success', duration)
    },
    error(message: string, duration?: number) {
      return this.show(message, 'error', duration)
    },
    info(message: string, duration?: number) {
      return this.show(message, 'info', duration)
    },
    remove(id: number) {
      const t = timers.get(id)
      if (t) {
        clearTimeout(t)
        timers.delete(id)
      }
      this.items = this.items.filter((it) => it.id !== id)
    },
  },
})
