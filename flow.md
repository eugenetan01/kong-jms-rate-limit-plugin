1. log phase to count number of bytes and add it to a counter ctx var
2. preread phase in next execution, will check the ctx connter

- if ctx counter exceed limit terminate with return ngx.exit(429)

Notes:

- no partial passthrough for a tcp connection
- either all 5 messages get sent in a single tcp, or none at all
