# clj-countdown-latch

Execute things asynchronously and wait for them all to finish before returning.

## Exmaple Usage

```
(require '[clj-countdown-latch.core :as cc])

(cc/with-countdown-latch 500 
  (dotimes [x 500]
    (cc/async-as-necessary 
      ;; slow things
      )))
```

## License

Copyright © 2019 Opportunity Financial, LLC

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
