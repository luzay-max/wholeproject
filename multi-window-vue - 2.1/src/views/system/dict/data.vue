<template>
  <div class="admin-page-container">
    <div class="admin-search-card">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-select v-model="queryParams.dictType" placeholder="字典名称" clearable style="width: 100%">
            <el-option
              v-for="item in typeOptions"
              :key="item.dictId"
              :label="item.dictName"
              :value="item.dictType"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="queryParams.dictLabel"
            placeholder="请输入字典标签"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="queryParams.status"
            placeholder="数据状态"
            clearable
            style="width: 100%"
          >
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-col>
        <el-col :span="6" class="btn-group">
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button @click="handleClose">返回</el-button>
        </el-col>
      </el-row>
    </div>

    <div class="admin-content-card admin-content-card--fixed-pagination">
      <div class="admin-table-header">
        <span class="admin-table-title">字典数据列表</span>
        <div class="admin-header-actions">
          <el-button type="primary" plain @click="handleAdd">新增</el-button>
          <el-button
            type="success"
            plain
            :disabled="single"
            @click="handleUpdate"
          >修改</el-button>
          <el-button
            type="danger"
            plain
            :disabled="multiple"
            @click="handleDelete"
          >删除</el-button>
        </div>
      </div>

      <div class="admin-table-wrapper">
        <el-table
          v-loading="loading"
          :data="dataList"
          class="admin-table"
          :fit="!isMobile"
          :table-layout="isMobile ? 'auto' : 'fixed'"
          @selection-change="handleSelectionChange"
          style="width: 100%"
        >
          <el-table-column v-if="!isMobile" type="selection" width="55" align="center" />
          <el-table-column v-if="!isMobile" label="字典编码" align="center" prop="dictCode" />
          <el-table-column label="字典标签" align="center" prop="dictLabel">
            <template #default="scope">
              <span
                v-if="!scope.row.listClass || scope.row.listClass === 'default'"
                :class="scope.row.cssClass"
              >{{ scope.row.dictLabel }}</span>
              <el-tag
                v-else
                :type="scope.row.listClass === 'primary' ? '' : scope.row.listClass"
                :class="scope.row.cssClass"
              >{{ scope.row.dictLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="字典键值" align="center" prop="dictValue" />
          <el-table-column v-if="!isMobile" label="字典排序" align="center" prop="dictSort" />
          <el-table-column label="状态" align="center" prop="status" :width="isMobile ? 84 : 100">
            <template #default="scope">
              <el-tag v-if="scope.row.status === '0'" type="success">正常</el-tag>
              <el-tag v-else type="danger">停用</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="!isMobile" label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
          <el-table-column v-if="!isMobile" label="创建时间" align="center" prop="createTime" width="180" />
          <el-table-column
            label="操作"
            align="center"
            class-name="small-padding fixed-width"
            :width="isMobile ? 128 : 150"
            :fixed="isMobile ? false : 'right'"
          >
            <template #default="scope">
              <el-button link type="primary" @click="handleUpdate(scope.row)">修改</el-button>
              <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="admin-pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="Number(total) || dataList.length"
          :page-count="pageCount"
          :hide-on-single-page="false"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="getList"
          @current-change="getList"
        />
      </div>
    </div>

    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="dataRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="字典类型">
          <el-input v-model="form.dictType" disabled />
        </el-form-item>
        <el-form-item label="数据标签" prop="dictLabel">
          <el-input v-model="form.dictLabel" placeholder="请输入数据标签" />
        </el-form-item>
        <el-form-item label="数据键值" prop="dictValue">
          <el-input v-model="form.dictValue" placeholder="请输入数据键值" />
        </el-form-item>
        <el-form-item label="样式属性" prop="cssClass">
          <el-input v-model="form.cssClass" placeholder="请输入样式属性" />
        </el-form-item>
        <el-form-item label="显示排序" prop="dictSort">
          <el-input-number v-model="form.dictSort" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="回显样式" prop="listClass">
          <el-select v-model="form.listClass" style="width: 100%">
            <el-option
              v-for="item in listClassOptions"
              :key="item.value"
              :label="`${item.label}(${item.value})`"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, toRefs, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listData, getData, delData, addData, updateData } from '../../../api/system/dict/data'
import { getType, optionselect as getDictOptionSelect } from '../../../api/system/dict/type'

defineOptions({
  name: 'DictData'
})

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const dataList = ref([])
const title = ref('')
const open = ref(false)
const dataRef = ref(null)
const defaultDictType = ref('')
const typeOptions = ref([])
const isMobile = ref(false)

const updateViewportState = () => {
  isMobile.value = window.innerWidth <= 768
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    dictName: undefined,
    dictType: undefined,
    status: undefined,
    dictLabel: undefined
  },
  rules: {
    dictLabel: [{ required: true, message: '数据标签不能为空', trigger: 'blur' }],
    dictValue: [{ required: true, message: '数据键值不能为空', trigger: 'blur' }],
    dictSort: [{ required: true, message: '数据顺序不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const pageCount = computed(() => {
  const totalCount = Number(total.value) || dataList.value.length || 0
  const size = Number(queryParams.value.pageSize) || 10
  return Math.max(1, Math.ceil(totalCount / size))
})

const listClassOptions = ref([
  { value: 'default', label: '默认' },
  { value: 'primary', label: '主要' },
  { value: 'success', label: '成功' },
  { value: 'info', label: '信息' },
  { value: 'warning', label: '警告' },
  { value: 'danger', label: '危险' }
])

function getTypes(dictId) {
  return getType(dictId).then((response) => {
    queryParams.value.dictType = response.data.dictType
    defaultDictType.value = response.data.dictType
  })
}

function getTypeList() {
  return getDictOptionSelect().then((response) => {
    typeOptions.value = response.data || []
  })
}

function getList() {
  loading.value = true
  listData(queryParams.value)
    .then((response) => {
      dataList.value = response?.data?.records || response?.data?.list || []
      total.value = Number(response?.data?.total ?? dataList.value.length ?? 0)
    })
    .finally(() => {
      loading.value = false
    })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    dictCode: undefined,
    dictLabel: undefined,
    dictValue: undefined,
    dictSort: 0,
    status: '0',
    remark: undefined,
    dictType: defaultDictType.value,
    listClass: 'default',
    cssClass: undefined
  }
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    dictType: defaultDictType.value,
    dictLabel: undefined,
    status: undefined
  }
  getList()
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加字典数据'
  form.value.dictType = queryParams.value.dictType
}

function handleUpdate(row) {
  reset()
  const dictCode = row?.dictCode || ids.value
  getData(dictCode).then((response) => {
    form.value = response.data
    open.value = true
    title.value = '修改字典数据'
  })
}

function submitForm() {
  if (!dataRef.value) return
  dataRef.value.validate((valid) => {
    if (!valid) return

    const request = form.value.dictCode != null ? updateData(form.value) : addData(form.value)
    const message = form.value.dictCode != null ? '修改成功' : '新增成功'

    request.then(() => {
      ElMessage.success(message)
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const dictCodes = row?.dictCode || ids.value
  ElMessageBox.confirm(`是否确认删除字典编码为"${dictCodes}"的数据项？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => delData(dictCodes))
    .then(() => {
      ElMessage.success('删除成功')
      getList()
    })
    .catch(() => {})
}

function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.dictCode)
  single.value = selection.length !== 1
  multiple.value = selection.length === 0
}

function handleClose() {
  router.push({ name: 'DictIndex' })
}

onMounted(async () => {
  updateViewportState()
  window.addEventListener('resize', updateViewportState)
  await getTypeList()

  const dictId = route.query.dictId
  const routeDictType = route.query.dictType

  if (dictId) {
    await getTypes(dictId)
  } else if (routeDictType) {
    queryParams.value.dictType = routeDictType
    defaultDictType.value = routeDictType
  }

  getList()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportState)
})
</script>

<style scoped>
.btn-group {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.admin-content-card--fixed-pagination {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
}

.admin-content-card--fixed-pagination .admin-table-wrapper {
  overflow: auto;
  min-height: 0;
}

.admin-content-card--fixed-pagination .admin-pagination {
  position: sticky;
  bottom: 0;
  padding-top: 8px;
  background: #fff;
  z-index: 3;
}

.admin-content-card--fixed-pagination :deep(.el-pagination__jump),
.admin-content-card--fixed-pagination :deep(.el-pagination__sizes) {
  display: inline-flex !important;
  align-items: center;
  visibility: visible !important;
}
</style>
