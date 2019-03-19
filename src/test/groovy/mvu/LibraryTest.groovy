package mvu

import spock.lang.Specification

class ModelViewBinderTest extends Specification {
    def "someLibraryMethod returns true"() {
        setup:
        def lib = new Library()

        when:
        def result = lib.someLibraryMethod()

        then:
        result == true
    }
}
