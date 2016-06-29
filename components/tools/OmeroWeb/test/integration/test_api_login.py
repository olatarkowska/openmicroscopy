#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

"""
Tests logging in with webgateway json api
"""

from weblibrary import IWebTest, _get_response_json, _post_response_json
from django.core.urlresolvers import reverse
from django.conf import settings


class TestLogin(IWebTest):
    """
    Tests login workflow: getting url, csfv tokens etc.
    """

    def test_versions(self):
        """
        Start at the base url, get versions
        """
        django_client = self.django_root_client
        request_url = reverse('api_versions')
        rsp = _get_response_json(django_client, request_url, {})
        versions = rsp['versions']
        assert len(versions) == len(settings.WEBGATEWAY_API_VERSIONS)
        for v in versions:
            assert v['version'] in settings.WEBGATEWAY_API_VERSIONS

    def test_base_url(self):
        """
        Tests that the base url for a given version provides other urls
        """
        django_client = self.django_root_client
        # test the most recent version
        version = settings.WEBGATEWAY_API_VERSIONS[-1]
        request_url = reverse('api_base', kwargs={'api_version': version})
        rsp = _get_response_json(django_client, request_url, {})
        assert 'servers_url' in rsp
        assert 'login_url' in rsp
        assert 'projects_url' in rsp

    def test_login_csrf(self):
        """
        Tests that we can only login with CSRF
        """
        django_client = self.django_root_client
        # test the most recent version
        version = settings.WEBGATEWAY_API_VERSIONS[-1]
        request_url = reverse('api_login', kwargs={'api_version': version})
        rsp = _post_response_json(django_client, request_url, {},
                             status_code=403)
        assert (rsp['message'] ==
                "CSRF Error. You need to include 'X-CSRFToken' in header")
