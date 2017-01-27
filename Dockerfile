FROM kbase/kbase:sdkbase.latest
MAINTAINER KBase Developer
# -----------------------------------------

# Insert apt-get instructions here to install
# any required dependencies for your module.

# RUN apt-get update

# -----------------------------------------

COPY ./ /kb/module
RUN mkdir -p /kb/module/work
RUN chmod -R 777 /kb/module

WORKDIR /kb/module
RUN keytool -import -keystore /usr/lib/jvm/java-7-oracle/jre/lib/security/cacerts -storepass changeit -noprompt -trustcacerts -alias letsencryptauthorityx3 -file ./ssl/lets-encrypt-x3-cross-signed.der

RUN make all

ENTRYPOINT [ "./scripts/entrypoint.sh" ]

CMD [ ]
