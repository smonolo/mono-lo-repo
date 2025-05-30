import { useQuery } from '@tanstack/vue-query'
import type { FilesResponse } from '~/types/screens/files'

export const useFiles = (logged: Ref<boolean>, page: Ref<number>) => {
  return useQuery({
    queryKey: ['filesScreen_files', logged, page],
    queryFn: async (): Promise<FilesResponse> => {
      const response = await fetch(
        `https://cdn.smnl.dev/list?page=${page.value}`,
        {
          headers: {
            authorization: localStorage.getItem('filesScreen_token') as string,
          },
        }
      )

      return await response.json()
    },
    refetchOnWindowFocus: false,
    enabled: logged,
  })
}
