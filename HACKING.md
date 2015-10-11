# Hacking on Agile Grenoble Backend

## Setting up the development environment

Clone the repo:

    git clone git@github.com:martinsson/agile-grenoble-backend.git
    cd agile-grenoble-backend/

Install [Leiningen](http://leiningen.org/):

    curl https://raw.github.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein
    chmod +x /usr/local/bin/lein

Start the server:

    lein ring server
