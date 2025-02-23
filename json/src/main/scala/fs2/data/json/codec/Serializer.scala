/*
 * Copyright 2022 Lucas Satabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fs2.data.json
package codec

import ast.Tokenizer

/** Tells how a value is serialized into Json. */
trait Serializer[A] {
  type Json
  implicit val tokenizer: Tokenizer[Json]
  def serialize(a: A): Json
}

object Serializer {
  type Aux[A, J] = Serializer[A] { type Json = J }
}
