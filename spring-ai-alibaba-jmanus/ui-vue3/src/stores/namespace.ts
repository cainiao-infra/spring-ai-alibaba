/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { ref } from 'vue'
import { defineStore } from 'pinia'

export const usenameSpaceStore = defineStore('namespace', () => {
  const namespace = ref<string>('default')
  function setCurrentNs(value: string) {
    namespace.value = value
  }

  const namespaces = ref<Array<{ name: string; id: string ;host?: string}>>([])
  function setNamespaces(datasource: Array<{ name: string; id: string }>) {
    namespaces.value = datasource
  }

  return { namespace, namespaces, setCurrentNs, setNamespaces }
})
