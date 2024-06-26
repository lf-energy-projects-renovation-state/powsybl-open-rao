# Open RAO
[![Actions Status](https://github.com/powsybl/powsybl-core/workflows/CI/badge.svg)](https://github.com/powsybl/powsybl-open-rao/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Aopen-rao&metric=coverage)](https://sonarcloud.io/component_measures?id=com.powsybl%3Aopen-rao&metric=coverage)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Aopen-rao&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.powsybl%3Aopen-rao)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
[![Slack](https://img.shields.io/badge/slack-powsybl-blueviolet.svg?logo=slack)](https://join.slack.com/t/powsybl/shared_invite/zt-rzvbuzjk-nxi0boim1RKPS5PjieI0rA)
[![Javadocs](https://www.javadoc.io/badge/com.powsybl/powsybl-open-rao.svg?color=blue)](https://www.javadoc.io/doc/com.powsybl/powsybl-open-rao)
[![ReadTheDocsStatus](https://readthedocs.org/projects/powsybl-openrao/badge/?version=stable)](https://powsybl.readthedocs.io/projects/openrao/en/stable/?badge=stable)

Open RAO (Remedial Action Optimizer) is an open-source toolbox that aims at providing a modular engine for remedial actions optimisation, part of the Linux Foundation Energy.

**powsybl-open-rao** repository contains the main features of Open RAO.

For detailed information about Open RAO toolbox, please refer to the [detailed documentation](https://powsybl.readthedocs.io/projects/openrao/en/stable/index.html).

This project and everyone participating in it is governed by the [PowSyBl Code of Conduct](https://github.com/powsybl/.github/blob/main/CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code. Please report unacceptable behavior to [powsybl-tsc@lists.lfenergy.org](mailto:powsybl-tsc@lists.lfenergy.org).

## Getting started

These instructions will get you a copy of the project up and running on your local machine
for development and testing purposes.

### Prerequisites

In order to build **powsybl-open-rao**, you need the following environment available:
  - Install JDK *(17 or greater)*,
  - Install Maven latest version.

### Installing

Open RAO needs a load flow implementation and a sensitivity analysis implementation at runtime, following the interfaces of **powsybl-core** which documentation is available in [PowSyBl website](https://www.powsybl.org/pages/documentation/). Note that for obvious reasons, included performances, reliability and transparency, Open RAO uses [Powsybl Open Load Flow](https://github.com/powsybl/powsybl-open-loadflow) by default, but you can prefer you own implementation.

### Running

In order for Open RAO to run without error, you will need to configure your *itools* platform. *itools* is a command line interface
provided by PowSyBl framework. 

Two options are available:
1.  First, you can use the one provided by Open RAO. It is saved in the *etc* directory of the installation, and is called *config.yml*.
You just have to copy-paste it in **$HOME/.itools** directory. You can also use one of the provided network post-processor scripts 
saved in the *etc* directory by copy-pasting them in the same directory and adding these lines to your *config.yml* file :

```$yml
import:
  postProcessors: groovyScript

groovy-post-processor:
    script: $HOME/.itools/core-cc-ucte-postProcessor.groovy

``` 

2.  Expert users can also adapt it to their own needs.

For using *itools*, enter on the command line:
 
```bash
cd <install-prefix>/bin
./itools help
```

For more information about *itools*, do not hesitate to visit [PowSyBl documentation](https://www.powsybl.org/docs/).

### Quick tutorial
If you prefer to get familiar with OpenRAO using a real example, take a look at this [tutorial](https://powsybl.readthedocs.io/projects/openrao/en/stable/tutorial.html).

## License

This project is licensed under the Mozilla Public License 2.0 - see the [LICENSE.txt](https://github.com/powsybl/powsybl-open-rao/blob/main/LICENSE.txt) file for details.
 
## Contributing

Please read the instructions [here](CONTRIBUTING.md).