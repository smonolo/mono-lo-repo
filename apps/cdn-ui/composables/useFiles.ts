import { useQuery } from '@tanstack/vue-query'

type File = {
  fileName: string
  size: number
  birthTime: string
}

type Response = {
  files: File[]
  totalSize: number
  totalFiles: number
  totalPages: number
}

export const useFiles = (page: Ref<number>) => {
  const { data, isFetching } = useQuery({
    queryKey: ['files', page],
    queryFn: async (): Promise<Response> => {
      const response = await fetch(
        `https://cdn.smnl.it/list?page=${page.value}`,
        {
          headers: {
            authorization: localStorage.getItem('authKey') as string,
          },
        }
      )

      return await response.json()
    },
    refetchOnWindowFocus: false,
  })

  return {
    data,
    isFetching,
  }
}
