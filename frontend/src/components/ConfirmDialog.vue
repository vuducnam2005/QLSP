<script setup lang="ts">
defineProps<{
  open: boolean
  productName: string
  busy: boolean
}>()

const emit = defineEmits<{
  cancel: []
  confirm: []
}>()
</script>

<template>
  <Transition name="fade">
    <div v-if="open" class="modal-backdrop" @click.self="emit('cancel')">
      <div class="confirm-dialog" role="alertdialog" aria-modal="true" aria-labelledby="confirm-title">
        <span class="warning-mark">!</span>
         <p class="eyebrow">Xóa mềm</p>
         <h2 id="confirm-title">Xóa {{ productName }}?</h2>
         <p>Sản phẩm sẽ rời khỏi danh mục đang hoạt động nhưng vẫn được lưu trong cơ sở dữ liệu.</p>
        <div class="confirm-actions">
           <button class="button secondary-button" type="button" :disabled="busy" @click="emit('cancel')">Giữ lại</button>
           <button class="button danger-button" type="button" :disabled="busy" @click="emit('confirm')">{{ busy ? 'Đang xóa...' : 'Xóa sản phẩm' }}</button>
        </div>
      </div>
    </div>
  </Transition>
</template>
