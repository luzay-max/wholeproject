<template>
  <el-select
    v-model="selectedValue"
    :placeholder="placeholder"
    :clearable="clearable"
    :disabled="disabled"
    :multiple="multiple"
    style="width: 100%"
  >
    <el-option
      v-for="item in internalOptions"
      :key="item.value || item.dictValue"
      :label="item.label || item.dictLabel"
      :value="item.value || item.dictValue"
      :disabled="item.status === '1'"
    >
      <slot name="option" :item="item">
        <span>{{ item.label || item.dictLabel }}</span>
      </slot>
    </el-option>
  </el-select>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { getDicts } from '../../api/system/dict/data'

const props = defineProps({
  modelValue: [String, Number, Array],
  dictType: {
    type: String,
    default: ''
  },
  options: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: '请选择'
  },
  clearable: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  },
  multiple: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedValue = ref(props.modelValue)
const internalOptions = ref([])

watch(() => props.modelValue, (val) => {
  selectedValue.value = val
})

watch(() => props.options, (val) => {
  internalOptions.value = val
}, { immediate: true })

watch(selectedValue, (val) => {
  emit('update:modelValue', val)
  emit('change', val)
})

onMounted(() => {
  if (props.options && props.options.length > 0) {
    internalOptions.value = props.options
  } else if (props.dictType) {
    getDicts(props.dictType).then(response => {
      internalOptions.value = response.data
    })
  }
})
</script>
