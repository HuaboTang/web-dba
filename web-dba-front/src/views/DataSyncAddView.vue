<script setup lang="ts">
// env
import { reactive, ref, watch } from 'vue'
import { ApiService } from '@/util/fetch'
import { ElMessage, type FormInstance } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

class DataSyncAddJob {
  id?: number
  name: string = ''
  cron: string = ''
  sourceDataSourceId?: number
  sourceSchema: string = ''
  sourceTable: string = ''
  targetDataSourceId?: number
  targetSchema: string = ''
  targetTable: string = ''
  orderByColumn: string = ''
}

class SchemeAndTable {
  schema: string = ''
  tables: string[] = []
}

let router = useRouter()
let route = useRoute()
const dataSyncJob = reactive<DataSyncAddJob>(new DataSyncAddJob())
const dataSyncJobFormRules = {
  name: { required: true, message: '请输入名称', trigger: 'blur' },
  cron: { required: true, message: '请输入cron表达式', trigger: 'blur' },
  sourceDataSourceId: { required: true, message: '请选择数据源', trigger: 'change' },
  sourceSchema: { required: true, message: '请输入源表schema', trigger: 'change' },
  sourceTable: { required: true, message: '请输入源表名', trigger: 'change' },
  targetDataSourceId: { required: true, message: '请选择目标数据源', trigger: 'change' },
  targetSchema: { required: true, message: '请输入目标表schema', trigger: 'change' },
  targetTable: { required: true, message: '请输入目标表名', trigger: 'change'}
}
const dataSources = ref<any[]>([])
const sourceSchemas = ref<SchemeAndTable[]>([])
const loadingSourceSchema = ref(false)
const sourceTables = ref<string[]>([])
const loadingSourceTable = ref(false)
const targetSchemas = ref<SchemeAndTable[]>([])
const loadingTargetSchema = ref(false)
const targetTables = ref<string[]>([])
const loadingTargetTable = ref(false)
const formRef = ref<FormInstance>()
// /env

// event
const initDataSyncJob = async () => {
  let jobId = route.query.jobId
  if (!jobId) {
    return
  }
  let resp = await ApiService.getInstance().get(`/api/data/sync/${jobId}`)
  Object.assign(dataSyncJob, resp.data.data)
  console.log('inited-data-sync-job')
  await onSourceDataSourceChange(dataSyncJob.sourceDataSourceId || 0)
  await onSourceSchemaChange(dataSyncJob.sourceSchema)
  await onTargetDataSourceChange(dataSyncJob.targetDataSourceId || 0)
  await onTargetSchemaChange(dataSyncJob.targetSchema)
}

const loadDataSources = async () => {
  let resp = await ApiService.getInstance().get('/api/datasource/all')
  dataSources.value = resp.data.data
}

const onSubmit = async () => {
  try {
    console.log('onSubmit', dataSyncJob)
    await formRef.value?.validate()
    let resp = await ApiService.getInstance().post('/api/data/sync/save', dataSyncJob)
    if (resp.data.result !== 200) {
      ElMessage({ message: resp.data.msg, type: 'error' })
      return
    }
    ElMessage({ message: '添加成功', type: 'success' })
    router.push({ path: '/data/sync' })
  } catch (e) {
    console.log(e)
  }
}

const onSourceDataSourceChange = async (sourceDataSourceId: number) => {
  loadingSourceSchema.value = true
  let resp = await ApiService.getInstance().get(`/api/metadata/schema-tables/${sourceDataSourceId}`)
  console.log('onSourceDataSourceChange', resp)
  let uniqueSchemas = resp.data.data.reduce((acc: any, item: any) => {
    let schema = item.schema
    let schemaAndTable
    if (!acc[schema]) {
      schemaAndTable = new SchemeAndTable()
      acc[schema] = schemaAndTable
    } else {
      schemaAndTable = acc[schema]
    }
    schemaAndTable.schema = schema
    schemaAndTable.tables.push(item.table)
    return acc
  }, {})
  console.log('onSourceSchemaChange', uniqueSchemas)
  sourceSchemas.value = Object.keys(uniqueSchemas).map(k => uniqueSchemas[k])
  loadingSourceSchema.value = false
}

const onTargetDataSourceChange = async (targetDataSourceId: number) => {
  loadingTargetSchema.value = true
  let resp = await ApiService.getInstance().get(`/api/metadata/schema-tables/${targetDataSourceId}`)
  console.log('onTargetDataSourceChange', resp)
  let uniqueSchemas = resp.data.data.reduce((acc: any, item: any) => {
    let schema = item.schema
    let schemaAndTable
    if (!acc[schema]) {
      schemaAndTable = new SchemeAndTable()
      acc[schema] = schemaAndTable
    } else {
      schemaAndTable = acc[schema]
    }
    schemaAndTable.schema = schema
    schemaAndTable.tables.push(item.table)
    return acc
  }, {})
  console.log('onTargetSchemaChange', uniqueSchemas)
  targetSchemas.value = Object.keys(uniqueSchemas).map(k => uniqueSchemas[k])
  loadingTargetSchema.value = false
}

const onSourceSchemaChange = async (sourceSchema: string) => {
  loadingSourceTable.value = true
  console.log('onSourceSchemaChange', sourceSchemas)
  sourceTables.value = sourceSchemas.value.filter(item => item.schema === sourceSchema)[0]?.tables
  loadingSourceTable.value = false
}

const onTargetSchemaChange = async (targetSchema: string) => {
  loadingTargetTable.value = true
  console.log('onTargetSchemaChange', targetSchemas)
  targetTables.value = targetSchemas.value.filter(item => item.schema === targetSchema)[0]?.tables
  loadingTargetTable.value = false
}
// /event

loadDataSources()
initDataSyncJob()

</script>

<template>
  <div style="width: 500px;">
  <el-form ref="formRef" :model="dataSyncJob" label-width="150" :rules="dataSyncJobFormRules">
    <el-form-item prop="name" label="名称">
      <el-input v-model="dataSyncJob.name" placeholder="请输入名称" />
    </el-form-item>
    <el-form-item prop="cron" label="cron表达式">
      <el-input v-model="dataSyncJob.cron" placeholder="请输入cron表达式" />
    </el-form-item>
    <el-form-item prop="sourceDataSourceId" label="源数据源">
      <el-select v-model="dataSyncJob.sourceDataSourceId"
                 @change="onSourceDataSourceChange"
                 placeholder="请选择数据源">
        <el-option v-for="item in dataSources" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
    </el-form-item>
    <el-form-item prop="sourceSchema" label="源表schema">
      <el-select v-model="dataSyncJob.sourceSchema"
                 placeholder="请输入源表schema"
                 @change="onSourceSchemaChange"
                 :loading="loadingSourceSchema">
        <el-option v-for="item in sourceSchemas" :key="item.schema" :label="item.schema" :value="item.schema" />
      </el-select>
    </el-form-item>
    <el-form-item prop="sourceTable" label="源表名">
      <el-select v-model="dataSyncJob.sourceTable" placeholder="请输入源表名">
        <el-option v-for="item in sourceTables" :key="item" :label="item" :value="item" />
      </el-select>
    </el-form-item>
    <el-form-item prop="targetDataSourceId" label="目标数据源">
      <el-select v-model="dataSyncJob.targetDataSourceId"
                 @change="onTargetDataSourceChange"
                 placeholder="请选择数据源">
        <el-option v-for="item in dataSources" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
    </el-form-item>
    <el-form-item prop="targetSchema" label="目标表schema">
      <el-select v-model="dataSyncJob.targetSchema"
                 @change="onTargetSchemaChange"
                 :loading="loadingTargetSchema"
                 placeholder="请输入目标表schema">
        <el-option v-for="item in targetSchemas" :key="item.schema" :label="item.schema" :value="item.schema" />
      </el-select>
    </el-form-item>
    <el-form-item prop="targetTable" label="目标表名">
      <el-select v-model="dataSyncJob.targetTable" placeholder="请输入目标表名">
        <el-option v-for="item in targetTables" :key="item" :label="item" :value="item" />
      </el-select>
    </el-form-item>
    <el-form-item label="排序字段">
      <el-input v-model="dataSyncJob.orderByColumn" placeholder="请输入排序字段"></el-input>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit">提交</el-button>
      <el-button type="info" @click="router.go(-1)">返回</el-button>
    </el-form-item>
  </el-form>
  </div>
</template>

<style lang="scss" scoped>
</style>