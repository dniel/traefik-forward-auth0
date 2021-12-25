/*
 * Copyright (c)
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

package dniel.forwardauth.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.Arrays
import javax.validation.constraints.NotEmpty

class Application {
    @NotEmpty
    lateinit var name: String

    @get:JsonIgnore
    var clientId: String = ""

    @get:JsonIgnore
    var clientSecret: String = ""
    var audience: String = ""
    var scope: String = "profile openid email"
    var redirectUri: String = ""
    var tokenCookieDomain: String = ""
    var restrictedMethods: Array<String> = arrayOf("DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT")
    var requiredPermissions: Array<String> = emptyArray()
    var claims: Array<String> = emptyArray()
    var returnTo: String = ""

    override fun toString(): String {
        return "Application(name='$name', clientId='$clientId', clientSecret='$clientSecret', audience='$audience', scope='$scope', redirectUri='$redirectUri', tokenCookieDomain='$tokenCookieDomain', restrictedMethods=${Arrays.toString(restrictedMethods)}, requiredPermissions=${Arrays.toString(requiredPermissions)}, claims=${Arrays.toString(claims)})"
    }

}