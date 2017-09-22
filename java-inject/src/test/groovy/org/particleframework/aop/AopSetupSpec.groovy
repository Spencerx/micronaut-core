/*
 * Copyright 2017 original authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.particleframework.aop

import org.particleframework.aop.internal.AopAttributes
import org.particleframework.context.BeanContext
import org.particleframework.context.DefaultBeanContext
import org.particleframework.inject.BeanDefinition
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Graeme Rocher
 * @since 1.0
 */
class AopSetupSpec extends Specification {

    @Unroll
    void "test AOP method invocation for method #method"() {
        given:
        BeanContext beanContext = new DefaultBeanContext().start()
        AopTargetClass foo = beanContext.getBean(AopTargetClass)

        expect:
        args.isEmpty() ? foo."$method"() : foo."$method"(*args) == result

        where:
        method        | args         | result
        'test'        | ['test']     | "Name is changed"                   // test for single string arg
        'test'        | [10]         | "Age is 20"                   // test for single primitive
        'test'        | ['test', 10] | "Name is changed and age is 10"    // test for multiple args, one primitive
        'test'        | []           | "noargs"                           // test for no args
        'testVoid'    | ['test']     | null                   // test for void return type
        'testVoid'    | ['test', 10] | null                   // test for void return type
        'testBoolean' | ['test']     | true                   // test for boolean return type
        'testBoolean' | ['test', 10] | true                  // test for boolean return type
        'testInt'     | ['test']     | 1                   // test for int return type
        'testInt'     | ['test', 10] | 20                  // test for int return type
        'testShort'   | ['test']     | 1                   // test for short return type
        'testShort'   | ['test', 10] | 20                  // test for short return type
        'testChar'    | ['test']     | 1                   // test for char return type
        'testChar'    | ['test', 10] | 20                  // test for char return type
        'testByte'    | ['test']     | 1                   // test for byte return type
        'testByte'    | ['test', 10] | 20                  // test for byte return type
        'testFloat'   | ['test']     | 1                   // test for float return type
        'testFloat'   | ['test', 10] | 20                  // test for float return type
        'testDouble'  | ['test']     | 1                   // test for double return type
        'testDouble'  | ['test', 10] | 20                  // test for double return type

    }


    void "test AOP setup"() {
        given:
        BeanContext beanContext = new DefaultBeanContext().start()

        when: "the bean definition is obtained"
        BeanDefinition<AopTargetClass> beanDefinition = beanContext.findBeanDefinition(AopTargetClass).get()

        then:
        beanDefinition.findMethod("test", String).isPresent()
        // should not be a reflection based method
        !beanDefinition.findMethod("test", String).get().getClass().getName().contains("Reflection")

        when:
        AopTargetClass foo = beanContext.getBean(AopTargetClass)


        then:
        foo instanceof Intercepted
        beanContext.findExecutableMethod(AopTargetClass, "test", String).isPresent()
        // should not be a reflection based method
        !beanContext.findExecutableMethod(AopTargetClass, "test", String).get().getClass().getName().contains("Reflection")
        foo.test("test") == "Name is changed"
        AopAttributes.@attributes.get() == null

    }

    void "test AOP setup attributes"() {
        given:
        BeanContext beanContext = new DefaultBeanContext().start()

        when:
        AopTargetClass foo = beanContext.getBean(AopTargetClass)
        def attrs = AopAttributes.get(AopTargetClass, "test", String)
        then:
        foo instanceof Intercepted
        foo.test("test") == "Name is changed"
        AopAttributes.@attributes.get().values().first().values == attrs

        when:
        AopAttributes.remove(AopTargetClass, "test", String)

        then:
        AopAttributes.@attributes.get() == null
    }
}