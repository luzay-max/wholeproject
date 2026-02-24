<template>
  <div class="dict-tag-container">
    <template v-for="(item, index) in matchedOptions" :key="(item.value || item.dictValue) + ''">
        <span
          v-if="isDefaultStyle(item)"
          :class="item.cssClass || item.css_class"
          class="dict-tag"
        >{{ item.label || item.dictLabel }}</span>
        <el-tag
          v-else
          :disable-transitions="true"
          :index="index"
          :type="(item.listClass || item.list_class) == 'primary' ? '' : (item.listClass || item.list_class)"
          :class="item.cssClass || item.css_class"
        >
          {{ item.label || item.dictLabel }}
        </el-tag>
    </template>

    <el-tag
      v-for="rawValue in unmatchedValues"
      :key="`raw-${rawValue}`"
      :disable-transitions="true"
      type="info"
      class="dict-tag"
    >
      {{ rawValue }}
    </el-tag>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 数据
  options: {
    type: Array,
    default: null,
  },
  // 当前的值
  value: {
    type: [Number, String, Array],
    default: null,
  },
})

const normalizedOptions = computed(() => {
  return Array.isArray(props.options) ? props.options : []
})

const normalizeValue = (value) => String(value ?? '').trim()
const normalizeCompareValue = (value) => normalizeValue(value).toLowerCase()

const getOptionValue = (item) => normalizeValue(item?.value ?? item?.dictValue)
const getComparableOptionValue = (item) => normalizeCompareValue(item?.value ?? item?.dictValue)

const isDefaultStyle = (item) => {
  const listClass = item?.listClass || item?.list_class
  return listClass === 'default' || listClass === '' || !listClass
}

const values = computed(() => {
  if (props.value !== null && typeof props.value !== 'undefined') {
    const valueList = Array.isArray(props.value) ? props.value : [props.value]
    return valueList.map((item) => normalizeValue(item)).filter((item) => item !== '')
  } else {
    return []
  }
})

const comparableValues = computed(() => {
  return values.value.map((item) => normalizeCompareValue(item))
})

const matchedOptions = computed(() => {
  return normalizedOptions.value.filter((item) => comparableValues.value.includes(getComparableOptionValue(item)))
})

const unmatchedValues = computed(() => {
  const missing = values.value.filter((value) => {
    const currentComparable = normalizeCompareValue(value)
    return !normalizedOptions.value.some((item) => getComparableOptionValue(item) === currentComparable)
  })
  return Array.from(new Set(missing))
})
</script>

<style scoped>
.dict-tag-container {
  display: inline-flex;
  align-items: center;
}
.dict-tag {
  margin-right: 5px;
}
</style>
