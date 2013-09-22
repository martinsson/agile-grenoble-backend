# Hacking on Agile Grenoble Backend

## Setting up the development environment

Clone the repo:

    git clone git@github.com:martinsson/agile-grenoble-backend.git
    cd agile-grenoble-backend/

Install [Leiningen](http://leiningen.org/):

    curl https://raw.github.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein
    chmod +x /usr/local/bin/lein

Generate the admin account password:

    mkdir ~/.sessions
    echo "admin: secret" > ~/.sessions/credentials.properties

Copy the sample sessions:

    cp resources/public/uploaded-sessions.csv ~/

Start the server:

    lein ring server
