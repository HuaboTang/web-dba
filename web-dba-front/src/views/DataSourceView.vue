<script setup lang="ts">

import {reactive, ref} from "vue";
import { ElMessage, type FormInstance } from 'element-plus'
import { ApiService } from '@/util/fetch'

class DataSource {
  id?: number
  type?: string
  name?: string
  url?: string
  username?: string
  password?: string
}

// refs
const dataSource = reactive<DataSource>({})
const dataSources = ref<DataSource[]>([])
const dataSourceDialogVisible = ref(false)
const dataSourceFormRules = ref({
  type: [{required: true, message: '请选择类型', trigger: 'blur'}],
  name: [{required: true, message: '请输入名称', trigger: 'blur'}],
  url: [{required: true, message: '请输入URL', trigger: 'blur'}],
  username: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  password: [{required: true, message: '请输入密码', trigger: 'blur'}]
})
const dataSourceForm = ref<FormInstance>()
// /refs

// events
const onAddDataSource = () => {
  dataSourceDialogVisible.value = true
  Object.assign(dataSource, {
    id: null, name: null, url: null, type: null, username: null, password: null
  })
}

const onTestConnection = async () => {
  try {
    console.log("onTestConnection")
    await dataSourceForm?.value?.validate()
    const respose = await ApiService.getInstance().post('/api/datasource/test/connection', dataSource)
    console.log("response:", respose)
    if (respose.data.data.success) {
      ElMessage({
        message: '连接成功',
        type: 'success'
      })
    } else {
      ElMessage({
        message: '连接失败: ' + respose.data.data.message,
        type: 'error'
      })
    }
  } catch (e) {
    console.log('form validate error', e)
  }
}

const onDataSourceFormSubmit = async () => {
  console.log("onDataSourceFormSubmit")
  await dataSourceForm?.value?.validate()
  const response = await ApiService.getInstance().post('/api/datasource/save', dataSource)
  console.log("save response:", response)
  if (response.data.result === 200) {
    ElMessage({ message: '添加成功', type: 'success'})
    dataSourceDialogVisible.value = false
    loadDataSource()
  } else {
    ElMessage({ message: '添加失败: ' + response.data.message, type: 'error'})
  }
}

const onEditDataSource = (record: DataSource) => {
  Object.assign(dataSource, record)
  dataSourceDialogVisible.value = true
  dataSourceForm?.value?.resetFields()
}

const onDeleteDataSource = async (record: DataSource) => {
  console.log("onDeleteDataSource", record)
  const response = await ApiService.getInstance().post('/api/datasource/delete/' + record.id)
  console.log("delete response:", response)
  if (response.data.result === 200) {
    dataSourceDialogVisible.value = false
    ElMessage({ message: '删除成功', type: 'success'})
    loadDataSource()
  } else {
    ElMessage({ message: '删除失败: ' + response.data.message, type: 'error'})
  }
}
// /events

// init
const loadDataSource = async () => {
  const response = await ApiService.getInstance().get('/api/datasource/all')
  dataSources.value = response.data.data
}
loadDataSource()

</script>

<template>
  <el-row :gutter="20">
    <el-col :span="6" v-for="item in dataSources" :key="item.name">
      <el-card class="box-card" @click="onEditDataSource(item)">
        {{item.name}}
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card @click="onAddDataSource">添加数据源</el-card>
    </el-col>
  </el-row>

  <el-drawer
      v-model="dataSourceDialogVisible"
      title="添加数据源"
      size="50%">
    <el-form
        :model="dataSource"
        ref="dataSourceForm"
        :rules="dataSourceFormRules"
        label-width="100px">
      <el-form-item label="名称" prop="name">
        <el-input v-model="dataSource.name"></el-input>
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="dataSource.type" placeholder="请选择类型">
          <el-option label="MySQL" value="MYSQL"></el-option>
          <el-option label="达梦" value="DM"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="URL" prop="url">
        <el-input v-model="dataSource.url"></el-input>
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="dataSource.username"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="dataSource.password"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onTestConnection">测试连接</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onDataSourceFormSubmit">添加</el-button>
      </el-form-item>
      <el-form-item v-if="dataSource.id">
        <el-button type="danger" @click="onDeleteDataSource(dataSource)">删除</el-button>
      </el-form-item>
    </el-form>
  </el-drawer>
</template>