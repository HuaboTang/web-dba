<script setup lang="ts">
// env
import { ref } from 'vue'
import { ApiService } from '@/util/fetch'
import { ElMessage, genFileId, type UploadInstance, type UploadProps, type UploadRawFile } from 'element-plus'

class DataSource {
  id?: number
  name?: string
}

const dataSources = ref<DataSource[]>([])
const schemaAndTables = ref<any[]>([])
const schemas = ref<String[]>([])
const tables = ref<String[]>([])
const upload = ref<UploadInstance>()

const currentDataSource = ref(0)
const currentSchema = ref('')
const currentTable = ref('')

const importDialogVisible = ref(false)
const sql = ref("")
const queryHeader = ref([])
const queryDatas = ref<any[]>([])
const executeAlertMessage = ref("")
const executeAlertType = ref('success')
// /env

const loadDataSource = async () => {
  let response = await ApiService.getInstance().get('/api/datasource/all')
  console.log('loadDataSource', response)
  dataSources.value = response.data.data
}

loadDataSource()

// events
const onDataSourceChange = async () => {
  try {
    schemas.value = []
    tables.value = []
    let axiosResponse = await ApiService.getInstance().get('/api/metadata/schema-tables/' + currentDataSource.value)
    let respData = axiosResponse.data.data
    schemaAndTables.value = respData

    let _schemas = respData.map((item: any) => item.schema)
    schemas.value = [...new Set<String>(_schemas)]
    currentSchema.value = _schemas[0]
  } catch (e) {
    console.log(e)
  }
}

const onSchemaChange = () => {
  let _currentSchema = currentSchema.value
  let _schemaAndTables = schemaAndTables.value
  let _tables = _schemaAndTables
    .filter((item: any) => item.schema == _currentSchema)
    .map((item: any) => item.table)
  tables.value = _tables
  currentTable.value = _tables[0]
}

const onTableChange = () => {
}

const onImport = () => {
  console.log('导入数据')
  importDialogVisible.value = true
}

const onGeneralImportTemplate = () => {
  console.log('生成导入模板')
}

const handleExceed: UploadProps['onExceed'] = (files) => {
  upload.value!.clearFiles()
  const file = files[0] as UploadRawFile
  file.uid = genFileId()
  upload.value!.handleStart(file)
}

const submitUpload = () => {
  upload.value!.submit()
}

const onUploadSuccess = (response: any) => {
  if (response.result == 200) {
    importDialogVisible.value = false
  }
}

const onUploadError = (error: Error) => {
  ElMessage({
    type: 'error',
    message: '导入失败：' + error.message
  })
}


const onExecuteSql = async () => {
  console.log('执行SQL', sql)
  if (!sql) {
    ElMessage({
      type: 'error',
      message: 'SQL不能为空'
    })
    return
  }
  try {
    let resp = await ApiService.getInstance().post('/api/data/execute/sql', {
      sql: sql.value,
      dataSourceId: currentDataSource.value
    })
    resp = resp.data
    console.log('execute-sql-resp', resp)
    if (resp.data.success) {
      executeAlertMessage.value = "执行成功"
      executeAlertType.value = 'success'
      queryHeader.value = resp.data.headers
      queryDatas.value = resp.data.datas.map((item: any) => {
        let row: { [Key: string]: any } = {}
        for (var i = 0; i < resp.data.headers.length; i++) {
          let header: string = resp.data.headers[i] as string
          row[header] = item[i]
        }
        return row
      })
    } else {
      executeAlertMessage.value = resp.data.message
      executeAlertType.value = 'error'
      queryHeader.value = []
      queryDatas.value = []
    }
  } catch (e) {
    console.log(e)
  }
}
// /events
</script>

<template>
  <el-form :inline="true">
    <el-form-item label="数据源">
      <el-select v-model="currentDataSource"
                 style="width: 150px"
                 @change="onDataSourceChange">
        <el-option v-for="item in dataSources"
                   :key="item.id"
                   :label="item.name"
                   :value="item.id"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="Schema">
      <el-select v-model="currentSchema"
                 style="width: 150px"
                 @change="onSchemaChange">
        <el-option v-for="item in schemas" :key="item" :label="item" :value="item"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="Table">
      <el-select v-model="currentTable"
                 style="width: 150px"
                 @change="onTableChange">
        <el-option v-for="item in tables" :key="item" :label="item" :value="item"></el-option>
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type=primary @click="onImport" :disabled="!currentTable">导入数据</el-button>
    </el-form-item>
    <el-form-item>
      <el-button type=primary @click="onGeneralImportTemplate" :disabled="!currentTable">生成导入模版</el-button>
    </el-form-item>
  </el-form>

  <div>
    <el-form>
    <el-form-item>
      <el-input type="textarea" rows="10" v-model="sql"></el-input>
    </el-form-item>
      <el-form-item>
        <el-button type=primary @click="onExecuteSql" :disabled="!currentTable">执行</el-button>
      </el-form-item>
    </el-form>
    <el-alert v-if="executeAlertMessage"
              :closable="false"
              :title="executeAlertMessage"
              :type="executeAlertType" style="margin-bottom: 18px;"/>
    <el-table :data="queryDatas" v-if="queryHeader && queryHeader.length > 0">
      <el-table-column v-for="item in queryHeader" :key="item" :prop="item" :label="item" />
    </el-table>
  </div>

  <el-dialog title="导入数据" v-model="importDialogVisible">
    <el-upload
      ref="upload"
      class="upload-demo"
      :action="'/api/data/import/' + currentDataSource + '/' + currentSchema + '/' + currentTable"
      :limit="1"
      accept=".xlsx,.xls"
      :on-exceed="handleExceed"
      :auto-upload="false"
      :on-success="onUploadSuccess"
      :on-error="onUploadError"
    >
      <template #trigger>
        <el-button type="primary">选择文件</el-button>
      </template>

      <el-button class="ml-3" type="success" @click="submitUpload">
        上传
      </el-button>
      <template #tip>
        <div class="el-upload__tip text-red">
          limit 1 file, new file will cover the old file
        </div>
      </template>
    </el-upload>
  </el-dialog>
</template>

<style scoped>

</style>