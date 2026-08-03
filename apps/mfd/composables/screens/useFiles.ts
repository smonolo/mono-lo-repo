import { useQuery } from '@tanstack/vue-query'
import type { FilesResponse } from '~/types/screens/files'

export const useFiles = (logged: Ref<boolean>, page: Ref<number>) => {
  return useQuery({
    queryKey: ['filesScreen_files', logged, page],
    queryFn: async (): Promise<FilesResponse> => {
      const token =
        typeof window !== 'undefined'
          ? localStorage.getItem('filesScreen_token')
          : ''

      const response = await fetch(
        `https://cdn.smnl.dev/list?page=${page.value}`,
        {
          headers: {
            authorization: token || '',
          },
        }
      )

      return await response.json()
    },
    refetchOnWindowFocus: false,
    enabled: logged,
  })
}
