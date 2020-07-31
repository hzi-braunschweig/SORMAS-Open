#!/bin/sh
### BEGIN INIT INFO
# Provides:          payara-sormas
# Required-Start:    postgresql
# Required-Stop:     postgresql
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Payara-Server SORMAS
### END INIT INFO

SORMAS_DOMAIN_DIR="/opt/domains/sormas"
PAYARA_USER="payara"

case "$1" in
start)
    su --login "$PAYARA_USER" --command "\"$SORMAS_DOMAIN_DIR/start-payara-sormas.sh\""
    ;;
stop)
    su --login "$PAYARA_USER" --command "\"$SORMAS_DOMAIN_DIR/stop-payara-sormas.sh\""
    ;;
restart)
    su --login "$PAYARA_USER" --command "\"$SORMAS_DOMAIN_DIR/stop-payara-sormas.sh\"; \"$SORMAS_DOMAIN_DIR/start-payara-sormas.sh\""
    ;;
*)
    echo "usage: $0 (start|stop|restart|help)"
esac
