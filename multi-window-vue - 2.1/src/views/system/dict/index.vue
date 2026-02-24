<template>
  <div class="admin-page-container">
    <!-- 筛选和搜索区域 -->
    <div class="admin-search-card">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input
            v-model="queryParams.dictName"
            placeholder="请输入字典名称"
            prefix-icon="Search"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-col>
        <el-col :span="6">
          <el-input
            v-model="queryParams.dictType"
            placeholder="请输入字典类型"
            prefix-icon="Search"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="queryParams.status"
            placeholder="字典状态"
            clearable
            style="width: 100%"
          >
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-col>
        <el-col :span="6" class="btn-group">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 字典列表 -->
    <div class="admin-content-card admin-content-card--fixed-pagination">
      <div class="admin-table-header">
        <span class="admin-table-title">字典类型列表</span>
        <div class="admin-header-actions">
          <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
          >修改</el-button>
          <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
          >删除</el-button>
        </div>
      </div>

      <div class="admin-table-wrapper">
        <el-table
          v-loading="loading"
          :data="typeList"
          class="admin-table"
          :fit="!isMobile"
          :table-layout="isMobile ? 'auto' : 'fixed'"
          @selection-change="handleSelectionChange"
          style="width: 100%"
        >
          <el-table-column v-if="!isMobile" type="selection" width="55" align="center" />
          <el-table-column v-if="!isMobile" label="字典编号" align="center" prop="dictId" width="100" />
          <el-table-column label="字典名称" align="center" prop="dictName" :show-overflow-tooltip="true" />
          <el-table-column label="字典类型" align="center" :show-overflow-tooltip="true">
            <template #default="scope">
              <el-link type="primary" @click="showData(scope.row)">{{ scope.row.dictType }}</el-link>
            </template>
          </el-table-column>
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
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
            <div class="admin-pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="Number(total) || typeList.length"
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

    <!-- 添加或修改字典类型对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="dictRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="form.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="form.dictType" placeholder="请输入字典类型" />
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

<script>
export default {
  name: "DictIndex"
}
</script>

<script setup>
import { ref, reactive, toRefs, onMounted, onBeforeUnmount, getCurrentInstance, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listType, getType, delType, addType, updateType } from '../../../api/system/dict/type'

const router = useRouter()
const { proxy } = getCurrentInstance() || {}

const loading = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const showSearch = ref(true)
const total = ref(0)
const typeList = ref([])
const title = ref('')
const open = ref(false)
const dictRef = ref(null)
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
    status: undefined
  },
  rules: {
    dictName: [{ required: true, message: "字典名称不能为空", trigger: "blur" }],
    dictType: [{ required: true, message: "字典类型不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const pageCount = computed(() => {
  const totalCount = Number(total.value) || typeList.value.length || 0
  const size = Number(queryParams.value.pageSize) || 10
  return Math.max(1, Math.ceil(totalCount / size))
})

/** 查询字典类型列表 */
function getList() {
  loading.value = true
  listType(queryParams.value).then(response => {
    typeList.value = response.data.records
    total.value = response.data.total
    // 同步更新列表数据
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    dictId: undefined,
    dictName: undefined,
    dictType: undefined,
    status: "0",
    remark: undefined
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    dictName: undefined,
    dictType: undefined,
    status: undefined
  }
  getList()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加字典类型"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const dictId = row.dictId || ids.value
  getType(dictId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改字典类型"
  })
}

/** 鎻愪氦鎸夐挳 */
function submitForm() {
  dictRef.value.validate(valid => {
    if (valid) {
      if (form.value.dictId != undefined) {
        updateType(form.value).then(response => {
          ElMessage.success("修改成功")
          open.value = false
          getList()
        })
      } else {
        addType(form.value).then(response => {
          ElMessage.success("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const dictIds = row.dictId || ids.value
  ElMessageBox.confirm('是否确认删除字典编号为 "' + dictIds + '" 的数据项？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(function() {
    return delType(dictIds)
  }).then(() => {
    getList()
    ElMessage.success("删除成功")
  }).catch(() => {})
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.dictId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 跳转到字典详情 */
function showData(row) {
  router.push({
    path: '/admin/dict/data',
    query: {
      dictId: row.dictId,
      dictType: row.dictType
    }
  })
}

onMounted(() => {
  updateViewportState()
  window.addEventListener('resize', updateViewportState)
  getList()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewportState)
})
</script>

<style scoped>
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


