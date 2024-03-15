import type { AxiosInstance, AxiosResponse } from 'axios'
import axios from 'axios'
import {ElMessage} from "element-plus";

export class CommonResponse<T> {
    result: number = 200
    msg?:string
    data?:T
}

export class ApiService {
    private static instance: ApiService
    private axiosInstance: AxiosInstance

    private constructor() {
        this.axiosInstance = axios.create({
            timeout: 1000 * 3 * 60,
            headers: {
                'Content-type': 'application/json;charset=UTF-8'
            },
            transformRequest: (data) => {
                return JSON.stringify(data)
            }
        })

        this.axiosInstance.interceptors.response.use((resp: AxiosResponse<CommonResponse<any>>) => {
            console.log("resp:", resp)
            if (resp.status !== 200 || resp.data.result !== 200)  {
                const errorMsg = resp.data?.msg || "服务异常"
                ElMessage({
                    message: errorMsg,
                    type: 'error'
                })
                throw Error(errorMsg)
            }
            return resp
        }, error => {
            console.log(error)
            ElMessage({
                message: '服务异常',
                type: 'error'
            })
            throw error
        })
    }

    public static getInstance(): AxiosInstance {
        if (!ApiService.instance) {
            ApiService.instance = new ApiService()
        }

        return ApiService.instance.axiosInstance
    }
}